package fr.lsmbo.organizer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Export {

    private static CellStyle styleHeader;
    private static CellStyle stylePercentage;

    private static void write(Row row, int index, double value) {
        write(row, index, value, false);
    }
    private static void write(Row row, int index, double value, boolean asPercentage) {
        row.createCell(index).setCellValue(value);
        if(asPercentage) row.getCell(index).setCellStyle(stylePercentage);
    }

    private static void write(Row row, int index, String value) {
        Double d = Utils.getNumeric(value);
        if(d != null) {
            // if it is numeric -> convert to double and write a number
            row.createCell(index).setCellValue(d);
        } else if(Utils.decimalSeparatorIsComma && (value.endsWith("%") || value.endsWith("Ko") || value.endsWith("Mo") || value.endsWith("Go") || value.endsWith("To"))) {
            // if it looks numeric and either % or file size -> replace the separator and write a string
            row.createCell(index).setCellValue(value.replace('.', ','));
        } else {
            // otherwise leave it like it is and write a string
            row.createCell(index).setCellValue(value);
        }
    }

    private static void writeHeaders(Sheet sheet, List<String> values) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < values.size(); i++) {
            row.createCell(i).setCellValue(values.get(i));
            row.getCell(i).setCellStyle(styleHeader);
        }
    }

    private static void write(Sheet sheet, int line, String key, double value) {
        Row row = sheet.createRow(line);
        row.createCell(0).setCellValue(key);
        write(row, 1, value);
    }
    private static void write(Sheet sheet, int line, String key, String value) {
        Row row = sheet.createRow(line);
        row.createCell(0).setCellValue(key);
        write(row, 1, value);
    }

    public static void createExcelReport(File outputFile, Folder folder) {
        Workbook workbook = new XSSFWorkbook();
        // generate styles
        styleHeader = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        styleHeader.setFont(font);
        stylePercentage = workbook.createCellStyle();
        stylePercentage.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));

        // generate sheets
        generateSheetGlobalInfo(workbook, folder);
        generateSheetPerContent(workbook, folder, false);
        generateSheetPerContent(workbook, folder, true);
        generateSheetPerFileType(workbook, folder);

        try {
            FileOutputStream fileOut = new FileOutputStream(outputFile.getAbsolutePath());
            workbook.write(fileOut);
            workbook.close();
            fileOut.close();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    private static void generateSheetGlobalInfo(Workbook workbook, Folder folder) {
        Sheet sheet = workbook.createSheet("Global information");
        // path, date, total size, used size, available size, nb files, nb sub directories
        write(sheet, 0, "Analyzed directory", folder.getPath().getAbsolutePath());
        write(sheet, 1, "Date", Utils.getFormattedDate()); // add formatted date
        write(sheet, 2, "Total used size", Utils.toHumanReadable(folder.getTotalSize()));
        write(sheet, 3, "Total used size in octets", folder.getTotalSize());
        write(sheet, 4, "Number of files", folder.getTotalNbFiles());
        write(sheet, 5, "Number of sub directories", folder.getTotalNbFolders());
        // autosize columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private static void generateSheetPerContent(Workbook workbook, Folder folder, boolean sizeInOctet) {
        Sheet sheet = workbook.createSheet("Data per directory"+(sizeInOctet ? " (in octets)" : ""));
        List<FileType> fileTypes = FileType.toList();
        // write formatted headers
        List<String> headers = new ArrayList<>(Arrays.asList("Directory", "Percentage", "Total size", "Number of files", "Number of sub directories"));
        headers.addAll(FileType.toListOfString());
        writeHeaders(sheet, headers);
        // write data
        int line = 1;
        for (Folder f : folder.getFolders()) {
            Row row = sheet.createRow(line++);
            int i = 0;
            double size = f.getTotalSize();
            write(row, i++, f.getPath().getName());
            write(row, i++, size/folder.getTotalSize(), true); // write percentage as a number with the right format
            if(sizeInOctet) {
                write(row, i++, size);
            } else {
                write(row, i++, Utils.toHumanReadable(size));
            }
            write(row, i++, f.getTotalNbFiles());
            write(row, i++, f.getTotalNbFolders());
            for(FileType ft : fileTypes) {
                if(sizeInOctet) {
                    write(row, i++, f.getTotalSizePerFileType().getOrDefault(ft, 0d));
                } else {
                    write(row, i++, Utils.toHumanReadable(f.getTotalSizePerFileType().getOrDefault(ft, 0d)));
                }
            }
        }
        // also add a line for root files
        Row row = sheet.createRow(line);
        int i = 0;
        write(row, i++, "<Files>");
        write(row, i++, folder.getCurrentSize()/folder.getTotalSize(), true); // write percentage as a number with the right format
        if(sizeInOctet) {
            write(row, i++, folder.getCurrentSize());
        } else {
            write(row, i++, Utils.toHumanReadable(folder.getCurrentSize()));
        }
        write(row, i++, folder.getCurrentSize());
        write(row, i++, folder.getCurrentNbFiles());
        for(FileType ft : fileTypes) {
            if(sizeInOctet) {
                write(row, i++, folder.getSizePerFileType().getOrDefault(ft, 0d));
            } else {
                write(row, i++, Utils.toHumanReadable(folder.getSizePerFileType().getOrDefault(ft, 0d)));
            }
        }
        // add autofilter
        sheet.setAutoFilter(new CellRangeAddress(0, folder.getFolders().size()+1, 0, headers.size()-1));
        // autosize columns
        for (i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void generateSheetPerFileType(Workbook workbook, Folder folder) {
        Sheet sheet = workbook.createSheet("Data per file type");
        List<FileType> fileTypes = FileType.toList();
        // write formatted headers
        List<String> headers = Arrays.asList("File type", "Total size", "Size in octets", "% Octets", "File count");
        writeHeaders(sheet, headers);
        // write data
        int line = 1;
        for (FileType ft : fileTypes) {
            double size = folder.getTotalSizePerFileType().getOrDefault(ft, 0d);
            Row row = sheet.createRow(line++);
            write(row, 0, ft.name());
            write(row, 1, Utils.toHumanReadable(size));
            write(row, 2, size);
            write(row, 3, size/folder.getTotalSize(), true); // write percentage as a number with the right format
            write(row, 4, folder.getTotalNbFilesPerFileType().getOrDefault(ft, 0d));
        }
        // add autofilter
        sheet.setAutoFilter(new CellRangeAddress(0, fileTypes.size(), 0, headers.size()-1));
        // autosize columns
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
