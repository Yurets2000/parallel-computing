package com.yube.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class FileUtils {

    private static FileUtils instance;

    private FileUtils() {}

    public static FileUtils getInstance() {
        if (instance == null) {
            instance = new FileUtils();
        }
        return instance;
    }

    public File getResourcesFile(String fileName) {
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("File not found!");
        } else {
            try {
                return new File(resource.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Illegal file name");
            }
        }
    }
}
