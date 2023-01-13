package com.yube.controller;

import com.yube.model.TaskProcessingType;
import com.yube.service.MicroserviceTaskProcessingService;
import com.yube.service.MonolithTaskProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/task-processing")
@RequiredArgsConstructor
public class TaskProcessingController {

    private final MicroserviceTaskProcessingService microserviceTaskSolvingService;
    private final MonolithTaskProcessingService monolithTaskSolvingService;

    @PostMapping("/loadTask")
    public String loadTask(@RequestParam("taskProcessingType") TaskProcessingType taskProcessingType, @RequestParam("file") MultipartFile file) throws Exception {
        if (taskProcessingType.equals(TaskProcessingType.MICROSERVICE)) {
            return microserviceTaskSolvingService.loadTask(file);
        } else {
            return monolithTaskSolvingService.loadTask(file);
        }
    }

    @GetMapping(value = "/taskResult/{taskId}", produces = "application/zip")
    public void getTaskResult(@PathVariable("taskId") String taskId, @RequestParam("taskProcessingType") TaskProcessingType taskProcessingType, HttpServletResponse response) throws Exception {
        if (taskProcessingType.equals(TaskProcessingType.MICROSERVICE)) {
            microserviceTaskSolvingService.getTaskResult(taskId, response);
        } else {
            monolithTaskSolvingService.getTaskResult(taskId, response);
        }
    }
}
