package com.yube.model;

import lombok.Data;

import java.util.List;

@Data
public class ZipFile {
    private final String name;
    private List<TxtFile> txtFiles;
}
