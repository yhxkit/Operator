package com.sample.operator.app.dto.common;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommonInfoDto
{
    LocalDateTime startTime;
    LocalDateTime endTime;

    long apiTime;
    long connTime;
    long sqlTime;

    boolean isSuccess;

    Object requestData;
    Object responseData;



}
