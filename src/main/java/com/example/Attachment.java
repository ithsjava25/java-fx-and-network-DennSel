package com.example;

public record Attachment(
        String name,
        String type,
        long size,
        String url
) {}

