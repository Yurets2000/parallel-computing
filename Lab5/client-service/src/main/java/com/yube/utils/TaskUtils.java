package com.yube.utils;

import com.yube.model.*;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TaskUtils {

    public static Task formTask(ZipFile zipFile) {
        List<TaskElement> taskElementList = new ArrayList<>();
        for (TxtFile txtFile : zipFile.getTxtFiles()) {
            TaskElement taskElement = new TaskElement(UUID.randomUUID().toString(), txtFile.getName(), txtFile.getContent());
            taskElementList.add(taskElement);
        }
        return new Task(UUID.randomUUID().toString(), zipFile.getName(), taskElementList);
    }

    public static TaskProcessingResult formTaskProcessingResult(Task task, List<TaskElementProcessingResult> taskElementProcessingResults) {
        TaskProcessingResult taskProcessingResult = new TaskProcessingResult();
        taskProcessingResult.setZipFileName(task.getName());
        List<TxtFile> txtFiles = new ArrayList<>();
        for (TaskElementProcessingResult taskElementProcessingResult : taskElementProcessingResults) {
            String txtFileName = task.getTaskElements().stream()
                    .filter(taskElement -> taskElement.getId().equals(taskElementProcessingResult.getTaskElementId()))
                    .findFirst()
                    .get()
                    .getName();
            TxtFile txtFile = new TxtFile(txtFileName);
            LinkedHashMap<String, Integer> sortedWordCountMap = taskElementProcessingResult.getSortedWordCountMap();
            StringBuilder txtFileContent = new StringBuilder();
            for (Map.Entry<String, Integer> wordCountMapEntry : sortedWordCountMap.entrySet()) {
                txtFileContent.append(wordCountMapEntry.getKey()).append(": ").append(wordCountMapEntry.getValue()).append("\n");
            }
            txtFile.setContent(txtFileContent.toString());
            txtFiles.add(txtFile);
        }
        taskProcessingResult.setTxtFiles(txtFiles);
        return taskProcessingResult;
    }

    public static void writeTaskProcessingResult(TaskProcessingResult taskProcessingResult, HttpServletResponse response) throws Exception {
        String zipFileName = taskProcessingResult.getZipFileName();
        List<TxtFile> txtFiles = taskProcessingResult.getTxtFiles();

        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", zipFileName));
        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (TxtFile txtFile : txtFiles) {
                ZipEntry zipEntry = new ZipEntry(txtFile.getName());
                zos.putNextEntry(zipEntry);
                zos.write(txtFile.getContent().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
    }
}
