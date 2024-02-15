package ru.practicum.shareit.booking;

import lombok.Data;

@Data
public class Review {
    private Long id;
    private String description;
    private boolean isTaskCompleted;
}
