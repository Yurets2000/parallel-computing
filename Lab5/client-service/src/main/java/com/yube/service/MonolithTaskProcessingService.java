package com.yube.service;

import com.yube.model.*;
import com.yube.utils.FileUtils;
import com.yube.utils.TaskUtils;
import com.yube.utils.TextUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MonolithTaskProcessingService {

    private final Queue<Task> tasks = new LinkedList<>();
    private final Map<String, TaskProcessingResult> taskProcessingResultMap = new HashMap<>();

    public String loadTask(MultipartFile file) throws Exception {
        ZipFile zipFile = FileUtils.readZipFile(file);
        Task task = TaskUtils.formTask(zipFile);
        tasks.add(task);
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

    @Scheduled(fixedRate = 200)
    public void processTask() {
        if (tasks.isEmpty()) return;
        Task currentTask = tasks.peek();
        if (currentTask == null) return;
        TaskProcessingResult taskProcessingResult = processTask(currentTask);
        log.info("Task processing with id={} completed", currentTask.getId());
        taskProcessingResultMap.put(currentTask.getId(), taskProcessingResult);
        tasks.remove();
    }

    private TaskProcessingResult processTask(Task task) {
        List<TaskElement> taskElements = task.getTaskElements();
        List<TaskElementProcessingResult> taskElementProcessingResults = new ArrayList<>();
        for (TaskElement taskElement : taskElements) {
            taskElementProcessingResults.add(processTaskElement(taskElement));
        }
        return TaskUtils.formTaskProcessingResult(task, taskElementProcessingResults);
    }

    private TaskElementProcessingResult processTaskElement(TaskElement taskElement) {
        TaskElementProcessingResult taskElementProcessingResult = new TaskElementProcessingResult();
        taskElementProcessingResult.setTaskElementId(taskElement.getId());
        LinkedHashMap<String, Integer> sortedWordCountMap = new LinkedHashMap<>();
        String content = taskElement.getContent()
                .toLowerCase(Locale.ROOT);
        if (StringUtils.isNotEmpty(content)) {
            String[] words = content.split("\\s");
            Map<String, Integer> wordCountMap = new HashMap<>();
            for (String word : words) {
                if (word.length() <= 1 || !word.matches(TextUtils.getWordRegex())) continue;
                if (!wordCountMap.containsKey(word)) {
                    wordCountMap.put(word, 0);
                }
                wordCountMap.put(word, wordCountMap.get(word) + 1);
            }
            List<WordCountToken> sortedWordCountTokens = wordCountMap.entrySet().stream()
                    .map(e -> new WordCountToken(e.getKey(), e.getValue()))
                    .sorted(WordCountToken::compareTo)
                    .collect(Collectors.toList());
            for (WordCountToken wordCountToken : sortedWordCountTokens) {
                sortedWordCountMap.put(wordCountToken.getWord(), wordCountToken.getCount());
            }
        }
        taskElementProcessingResult.setSortedWordCountMap(sortedWordCountMap);
        return taskElementProcessingResult;
    }
}
