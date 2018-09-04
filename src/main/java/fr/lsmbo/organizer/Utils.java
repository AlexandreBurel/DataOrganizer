package fr.lsmbo.organizer;

import java.io.File;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static boolean decimalSeparatorIsComma = false;
    private static final double KO = 1024L;
    private static final double MO = KO*1024L;
    private static final double GO = MO*1024L;
    private static final double TO = GO*1024L;

    public static File getCurrentDirectory() {
        return new File(System.getProperty("user.dir"));
    }

    public static File getOutputFile() {
        return new File(getCurrentDirectory(), "DataOrganizer-"+getFormattedDate(true)+".xlsx");
    }

    public static int getSubdirectoriesCount(File path) {
        int nbDirectories = 0;
        try {
            for (File file : path.listFiles()) {
                if (file.isDirectory()) nbDirectories++;
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
        return  nbDirectories;
    }

    public static boolean isValidPath(File path) {
        boolean validPath = false;
        if(!path.exists()) System.out.println("The given directory does not exist");
        else if(path.isDirectory()) {
            System.out.println("The following directory will be analyzed: " + path.getAbsolutePath());
            validPath = true;
        } else System.out.println("The given path is not a directory");
        return validPath;
    }

    public static boolean isValidOutput(File outputFile) {
        boolean validOutput = false;
        if(outputFile == null) {
            System.out.println("The Excel output file path is missing");
        } else if(outputFile.isFile() && outputFile.canWrite()) {
            System.out.println("The Excel output file path will be " + outputFile.getAbsolutePath());
            validOutput = true;
        } else {
            System.out.println("The Excel output file is not valid");
        }
        return validOutput;
    }

    public static void checkDecimalSeparator() {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        if(dfs.getDecimalSeparator() == ',') {
            decimalSeparatorIsComma = true;
        }
    }

    public static Double getNumeric(String value) {
        try {
            return Double.parseDouble(value);
        } catch(Throwable t) {
            return null;
        }
    }

    public static double roundedDiv(double value, double total, int nbDecimals) {
        double div = value / total; // something like 37.66546543...
        double x = Math.pow(10, nbDecimals); // if 0 decimals, 10^0 = 1
        return Math.round(div * x) / x;
    }

    public static String toHumanReadable(double bytes) {
        if(bytes < KO) { return ""+bytes; }
        else if(bytes < MO) { return roundedDiv(bytes, KO, 2)+"Ko"; }
        else if(bytes < GO) { return roundedDiv(bytes, MO, 2)+"Mo"; }
        else if(bytes < TO) { return roundedDiv(bytes, GO, 2)+"Go"; }
        else { return roundedDiv(bytes, TO, 2)+"To"; }
    }

    public static String getFormattedDate() {
        return getFormattedDate(false);
    }

    public static String getFormattedDate(Boolean shortFormat) {
        Date today = new Date();
        String pattern = shortFormat ? "yyyyMMdd-HHmmss" : "dd/MM/yyyy HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(today);
    }

    public static FileType getFileType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".sky") || name.endsWith(".skyd") || name.endsWith(".blib")) return FileType.Skyline;
        else if (name.endsWith(".dat") || name.endsWith(".mzid") || name.endsWith(".omx") || name.endsWith(".omx.bz2") || name.endsWith(".t.xml")) return FileType.Identification;
        else if (name.endsWith(".txt") || name.endsWith(".tmp") || name.endsWith(".xml") || name.endsWith(".csv")) return FileType.Text;
        else if (name.endsWith(".raw") || name.endsWith(".scan") || name.endsWith(".baf") || name.endsWith(".yep") || name.endsWith(".mzdb") || name.endsWith(".wiff")) return FileType.RawFile;
        else if (name.endsWith(".mgf") || name.endsWith(".mzxml") || name.endsWith(".mzml")) return FileType.Peaklist;
        else if (name.endsWith(".sf3")) return FileType.Scaffold;
        else if (name.endsWith(".fasta") || name.endsWith(".protdb")) return FileType.ProteinBank;
        else if (name.endsWith(".exe") || name.endsWith(".msi")) return FileType.Executable;
        else if (name.endsWith(".zip") || name.endsWith(".gz") || name.endsWith(".tgz") || name.endsWith(".rar") || name.endsWith(".7z")) return FileType.Archive;
        else if (name.endsWith(".xlsx") || name.endsWith(".xls")) return FileType.Excel;
        else if (name.endsWith(".pptx") || name.endsWith(".ppt")) return FileType.PowerPoint;
        else if (name.endsWith(".doc") || name.endsWith(".docx")) return FileType.Word;
        else if (name.endsWith(".pdf")) return FileType.PDF;
        else if (name.endsWith(".mp4") || name.endsWith(".avi")) return FileType.Video;
        else if (name.endsWith(".jpg") || name.endsWith(".tif") || name.endsWith(".bmp")) return FileType.Image;
        else return FileType.Other;
    }

}
