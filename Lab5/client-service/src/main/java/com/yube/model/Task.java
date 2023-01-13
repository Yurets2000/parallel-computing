package com.yube.model;

import lombok.Data;

import java.util.List;

@Data
public class Task {
    private final String id;
    private final String name;
    private final List<TaskElement> taskElements;
}
