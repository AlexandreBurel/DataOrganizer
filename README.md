# DataOrganizer
This is a simple tool I needed to get detailled information about the disk space used and available. Other tools exist with nice graphic output, but I needed an Excel output for my convenience.
The specificity of this tool is that it gathers files in 16 categories (Excel, Text, Executable, ...) including 6 mass spectrometry categories (raw files, peaklists, ...)

# Usage
It takes no argument, just copy the jar file in a specific directory and double-click on it to generate an Excel file.

# Compilation
Compile it with "mvn clean install" command, it should generate an executable jar file in the target subdirectory.
