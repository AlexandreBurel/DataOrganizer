package fr.lsmbo.organizer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Folder {
    private File path;
    private double currentSize = 0d;
    private double currentNbFiles = 0d;
    private double totalSize = -1d;
    private double totalNbFiles = -1d;
    private double totalNbFolders = -1d;
    private ArrayList<Folder> folders;
    private HashMap<FileType, Double> nbFilesPerFileType;
    private HashMap<FileType, Double> sizePerFileType;
    private boolean displayProgress = false;

    // recursive counts
    private HashMap<FileType, Double> recNbFilesPerFileType;
    private HashMap<FileType, Double> recSizePerFileType;

    public  Folder(File path) {
        this(path, false);
    }
    public Folder(File path, boolean displayProgress) {
        this.path = path;
        folders = new ArrayList<>();
        nbFilesPerFileType = new HashMap<>();
        sizePerFileType = new HashMap<>();
        String lastItemRead = "";
        try {
            int nbDirectories = displayProgress ? Utils.getSubdirectoriesCount(path) : -1;
            int nbDirectoriesRead = 0;
            for (File file : path.listFiles()) {
                lastItemRead = file.getAbsolutePath();
                if (file.isDirectory()) {
                    if(displayProgress) System.out.print("Reading directory '"+file.getName()+"'...");
                    try {
                        // recursive call
                        folders.add(new Folder(file));
                    } catch(NullPointerException e) {
                        // this happens when a directory is not accessible (restricted access perhaps)
                        System.out.print(" => Not readable");
                    }
                    if(displayProgress) System.out.println(" ["+Utils.roundedDiv(++nbDirectoriesRead*100, nbDirectories, 1)+"%]");
                } else {
                    FileType fileType = Utils.getFileType(file);
                    double nb = nbFilesPerFileType.getOrDefault(fileType, 0d);
                    nbFilesPerFileType.put(fileType, nb + 1);
                    double size = sizePerFileType.getOrDefault(fileType, 0d);
                    sizePerFileType.put(fileType, size + file.length());
                    currentNbFiles++;
                    currentSize += file.length();
                }
            }
            if(displayProgress) System.out.println("Progress: 100%");
        } catch(Throwable t) {
            // display the last folder read
            System.err.println("Error while reading, the last item read successfully is '"+lastItemRead+"'");
            t.printStackTrace();
        }
    }

    public File getPath() {
        return path;
    }

    public double getCurrentSize() {
        return currentSize;
    }

    public double getTotalSize() {
        if(totalSize == -1d) {
            // initialize size
            totalSize = currentSize;
            // add sizes from the sub folders (which will call this method recursively)
            for(Folder f : folders) {
                totalSize += f.getTotalSize();
            }
        }
        return totalSize;
    }

    public double getCurrentNbFiles() {
        return currentNbFiles;
    }

    public double getTotalNbFiles() {
        if(totalNbFiles == -1d) {
            // initialize size
            totalNbFiles = currentNbFiles;
            // add counts from the sub folders (which will call this method recursively)
            for(Folder f : folders) {
                totalNbFiles += f.getTotalNbFiles();
            }
        }
        return totalNbFiles;
    }

    public ArrayList<Folder> getFolders() {
        return folders;
    }

    public double getTotalNbFolders() {
        if(totalNbFolders == -1d) {
            // initialize size
            totalNbFolders = folders.size();
            // add counts from the sub folders (which will call this method recursively)
            for(Folder f : folders) {
                totalNbFolders += f.getTotalNbFolders();
            }
        }
        return totalNbFolders;
    }

//    public HashMap<FileType, Double> getNbFilesPerFileType() {
//        return nbFilesPerFileType;
//    }

    public HashMap<FileType, Double> getTotalNbFilesPerFileType() {
        if(recNbFilesPerFileType == null) {
            recNbFilesPerFileType = new HashMap<>();
            for(FileType ft : FileType.toList()) {
                // add values from current folder
                double value = nbFilesPerFileType.getOrDefault(ft, 0d);
                // add values from sub folders (recursive lazy call)
                for(Folder f : folders) {
                    value += f.getTotalNbFilesPerFileType().getOrDefault(ft, 0d);
                }
                recNbFilesPerFileType.put(ft, value);
            }
        }
        return recNbFilesPerFileType;
    }

    public HashMap<FileType, Double> getSizePerFileType() {
        return sizePerFileType;
    }

    public HashMap<FileType, Double> getTotalSizePerFileType() {
        if(recSizePerFileType == null) {
            recSizePerFileType = new HashMap<>();
            for(FileType ft : FileType.toList()) {
                // add values from current folder
                double value = sizePerFileType.getOrDefault(ft, 0d);
                // add values from sub folders (recursive lazy call)
                for(Folder f : folders) {
                    value += f.getTotalSizePerFileType().getOrDefault(ft, 0d);
                }
                recSizePerFileType.put(ft, value);
            }
        }
        return recSizePerFileType;
    }
}
