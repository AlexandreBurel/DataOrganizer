package fr.lsmbo.organizer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum FileType {
    Identification,
    Skyline,
    RawFile,
    Peaklist,
    ProteinBank,
    Text,
    Archive,
    Excel,
    Image,
    Video,
    PowerPoint,
    PDF,
    Word,
    Scaffold,
    Executable,
    Other;

    public static List<String> toListOfString() {
        return Stream.of(FileType.values()).sorted().map(FileType::name).collect(Collectors.toList());
    }
    public static List<FileType> toList() {
        return Stream.of(FileType.values()).sorted().collect(Collectors.toList());
    }
}
