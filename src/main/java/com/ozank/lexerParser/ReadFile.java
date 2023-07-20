package com.ozank.lexerParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
public class ReadFile {
    // Reads the entire contents of a file using the static method readString() of the {@link Files} class.
    public static String readFile(String filepath) {
        Path path = Path.of(filepath);
        List<String> lines = null;
        try {
            if (Files.exists(path)) {
                return Files.readString(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}