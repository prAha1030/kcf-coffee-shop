package com.kcfcoffeeshop.common.exception;

public record FieldErrorResponse(
        String field,
        String message
) {
}
