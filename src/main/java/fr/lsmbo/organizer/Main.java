package fr.lsmbo.organizer;

import java.awt.*;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        Utils.checkDecimalSeparator();

        try {
            System.out.println("Analysis started at "+Utils.getFormattedDate());

            System.out.println("Parsing directory...");
            Folder folder = new Folder(Utils.getCurrentDirectory(), true);

            System.out.println("Writing output...");
            File outputFile = Utils.getOutputFile();
            Export.createExcelReport(outputFile, folder);

            System.out.println("Opening output...");
            Desktop desktop = Desktop.getDesktop();
            desktop.open(outputFile);

            System.out.println("Analysis ended at "+Utils.getFormattedDate());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
