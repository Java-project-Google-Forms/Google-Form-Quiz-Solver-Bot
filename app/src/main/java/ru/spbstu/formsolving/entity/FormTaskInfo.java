package ru.spbstu.formsolving.entity;

public record FormTaskInfo(Long chatId, String formUrl, FormStructure structure) {
}