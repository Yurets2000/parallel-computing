package com.yube.service;

import com.yube.model.*;
import com.yube.utils.FileUtils;
import com.yube.utils.TaskUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
@EnableAsync
public class MicroserviceTaskProcessingService {

    @Value("${services.text-counting-service.url}")
    private String textCountingServiceUrl;
    @Value("${services.token-sorting-service.url}")
    private String tokenSortingServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Object>>> taskStagesResultMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TaskProcessingResult> taskProcessingResultMap = new ConcurrentHashMap<>();

    public String loadTask(MultipartFile file) throws Exception {
        ZipFile zipFile = FileUtils.readZipFile(file);
        Task task = TaskUtils.formTask(zipFile);
        tasks.add(task);
        ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Object>> stageResultMap = new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<Object> taskElementObjects = new ConcurrentLinkedQueue<>(task.getTaskElements());
        stageResultMap.put(0, taskElementObjects);
        taskStagesResultMap.put(task.getId(), stageResultMap);
        log.info("Task with id={} have been put on queue for processing", task.getId());
        return task.getId();
    }

    public void getTaskResult(String taskId, HttpServletResponse response) throws Exception {
        TaskProcessingResult taskProcessingResult = taskProcessingResultMap.get(taskId);
        taskProcessingResultMap.remove(taskId);
        if (taskProcessingResult == null) {
            throw new IllegalArgumentException("Task result by passed taskId not found");
        }
        TaskUtils.writeTaskProcessingResult(taskProcessingResult, response);
    }

    @Async("defaultTaskExecutor")
    @Scheduled(fixedRate = 200)
    public void processFirstStage() {
        if (tasks.isEmpty()) return;
        Task currentTask = tasks.peek();
        ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Object>> currentTaskStageResultMap = taskStagesResultMap.get(currentTask.getId());
        ConcurrentLinkedQueue<Object> currentTaskElementObjects = currentTaskStageResultMap.get(0);
        if (currentTaskElementObjects == null || currentTaskElementObjects.isEmpty()) return;
        log.info("Current task elements count: {}", currentTaskElementObjects.size());
        TaskElement currentElement = (TaskElement) currentTaskElementObjects.poll();
        if (currentElement == null) return;
        FirstStageProcessingResult firstStageProcessingResult;
        try {
            firstStageProcessingResult = processFirstStage(currentElement);
            firstStageProcessingResult.setTaskElementId(currentElement.getId());
        } catch (Exception e) {
            log.error("Exception during sending request to text-counting-service", e);
            return;
        }
        ConcurrentLinkedQueue<Object> firstStageProcessingResultObjects;
        if (!currentTaskStageResultMap.containsKey(1)) {
            firstStageProcessingResultObjects = new ConcurrentLinkedQueue<>();
        } else {
            firstStageProcessingResultObjects = currentTaskStageResultMap.get(1);
        }
        firstStageProcessingResultObjects.add(firstStageProcessingResult);
        currentTaskStageResultMap.put(1, firstStageProcessingResultObjects);
    }

    @Async("defaultTaskExecutor")
    @Scheduled(fixedRate = 200)
    public void processSecondStage() {
        if (tasks.isEmpty()) return;
        Task currentTask = tasks.peek();
        ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Object>> currentTaskStageResultMap = taskStagesResultMap.get(currentTask.getId());
        ConcurrentLinkedQueue<Object> currentFirstStageProcessingResultObjects = currentTaskStageResultMap.get(1);
        if (currentFirstStageProcessingResultObjects == null || currentFirstStageProcessingResultObjects.isEmpty())
            return;
        log.info("First stage processing results count: {}", currentFirstStageProcessingResultObjects.size());
        FirstStageProcessingResult firstStageProcessingResult = (FirstStageProcessingResult) currentFirstStageProcessingResultObjects.poll();
        if (firstStageProcessingResult == null) return;
        SecondStageProcessingResult secondStageProcessingResult;
        try {
            secondStageProcessingResult = processSecondStage(firstStageProcessingResult);
            secondStageProcessingResult.setTaskElementId(firstStageProcessingResult.getTaskElementId());
        } catch (Exception e) {
            log.error("Exception during sending request to text-counting-service", e);
            return;
        }
        ConcurrentLinkedQueue<Object> secondStageProcessingResultObjects;
        if (!currentTaskStageResultMap.containsKey(2)) {
            secondStageProcessingResultObjects = new ConcurrentLinkedQueue<>();
        } else {
            secondStageProcessingResultObjects = currentTaskStageResultMap.get(2);
        }
        secondStageProcessingResultObjects.add(secondStageProcessingResult);
        currentTaskStageResultMap.put(2, secondStageProcessingResultObjects);
        log.info("Second stage processing results count: {}", secondStageProcessingResultObjects.size());

        if (secondStageProcessingResultObjects.size() == currentTask.getTaskElements().size()) {
            List<TaskElementProcessingResult> taskElementProcessingResults = new ArrayList<>();
            for (Object secondStageProcessingResultObject : secondStageProcessingResultObjects) {
                taskElementProcessingResults.add((TaskElementProcessingResult) secondStageProcessingResultObject);
            }
            TaskProcessingResult taskProcessingResult = TaskUtils.formTaskProcessingResult(currentTask, taskElementProcessingResults);
            taskProcessingResultMap.put(currentTask.getId(), taskProcessingResult);
            log.info("Task processing with id={} completed", currentTask.getId());
            taskStagesResultMap.remove(currentTask.getId());
            tasks.remove();
        }
    }

    private FirstStageProcessingResult processFirstStage(TaskElement taskElement) {
        String countWordsUrl = textCountingServiceUrl + "/text-counting/word-count";
        return restTemplate.postForEntity(countWordsUrl, taskElement, FirstStageProcessingResult.class).getBody();
    }

    private SecondStageProcessingResult processSecondStage(FirstStageProcessingResult firstStageProcessingResult) {
        String sortTokensUrl = tokenSortingServiceUrl + "/token-sorting/sort-word-tokens";
        return restTemplate.postForEntity(sortTokensUrl, firstStageProcessingResult, SecondStageProcessingResult.class).getBody();
    }
}
