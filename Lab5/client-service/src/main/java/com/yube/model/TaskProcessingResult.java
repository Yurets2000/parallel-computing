package com.yube.model;

import lombok.Data;

import java.util.List;

@Data
public class TaskProcessingResult {
    private String zipFileName;
    private List<TxtFile> txtFiles;
}
