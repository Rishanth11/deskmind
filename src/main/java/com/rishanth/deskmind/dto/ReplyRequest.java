package com.rishanth.deskmind.dto;
import lombok.Data;

@Data
public class ReplyRequest {
    private String message;
    private boolean internal; // Maps to your isInternal flag
}