package com.kakao.api.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private ErrorHandling error;

    public ApiException(ErrorHandling e) {
        super(e.getMessage());
        this.error = e;
    }
}