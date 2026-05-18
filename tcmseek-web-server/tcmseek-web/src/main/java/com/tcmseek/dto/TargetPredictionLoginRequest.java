package com.tcmseek.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TargetPredictionLoginRequest implements Serializable {
    private String userName;
}
