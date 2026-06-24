package com.rishanth.deskmind.dto;

import lombok.Data;

@Data
public class AiClassificationResult {
    private String category;
    private String priority;
    private Integer confidence;
    private String reply;
}