package com.andruy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskId {
    private String id;
    private String name;
    private String time;
}
