package com.gym.gymtracker.exception;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ApiValidationError {
    String field;
    Object rejectedValue;
    String message;
}
