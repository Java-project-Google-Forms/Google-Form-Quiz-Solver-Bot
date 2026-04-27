package ru.spbstu.formsolving.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@AllArgsConstructor
public class FormStructure  {
    private String title;
    private String description;
    private List<Question> questions;
}