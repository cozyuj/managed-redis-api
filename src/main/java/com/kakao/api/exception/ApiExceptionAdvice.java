package com.kakao.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class}, basePackages = {"com.example.HyThon.web.controller"})
public class ApiExceptionAdvice {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResult> handleRuntimeException(HttpServletRequest request, RuntimeException e) {
        log.error("RuntimeException: {}", e.getMessage(), e);
        return buildResponse(ErrorHandling.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResult> handleBadRequest(HttpServletRequest request, BadRequestException e) {
        log.error("BadRequestException: {}", e.getMessage(), e);
        return buildResponse(ErrorHandling.INVALID_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult> handleAccessDenied(HttpServletRequest request, AccessDeniedException e) {
        log.error("AccessDeniedException: {}", e.getMessage(), e);
        return buildResponse(ErrorHandling.FORBIDDEN);
    }

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ResponseEntity<ApiResult> handleForbidden(HttpServletRequest request, HttpClientErrorException.Forbidden e) {
        log.error("Forbidden: {}", e.getMessage(), e);
        return buildResponse(ErrorHandling.FORBIDDEN);
    }

    @ExceptionHandler(HttpServerErrorException.InternalServerError.class)
    public ResponseEntity<ApiResult> handleInternalServerError(HttpServletRequest request, HttpServerErrorException.InternalServerError e) {
        log.error("InternalServerError: {}", e.getMessage(), e);
        return buildResponse(ErrorHandling.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ApiResult> handleTooManyRequests(HttpServletRequest request, HttpClientErrorException.TooManyRequests e) {
        log.error("TooManyRequests: {}", e.getMessage(), e);
        return buildResponse(ErrorHandling.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResult> handleApiException(HttpServletRequest request, ApiException e) {
        log.error("ApiException: {}", e.getMessage(), e);
        ApiExceptionEntity entity = ApiExceptionEntity.builder()
                .errorCode(e.getError().getCode())
                .errorMessage(e.getError().getMessage())
                .build();

        return ResponseEntity
                .status(e.getError().getHttpStatus())
                .body(ApiResult.builder()
                        .status("ERROR")
                        .message(e.getError().getMessage())
                        .exception(entity)
                        .build());
    }


    private ResponseEntity<ApiResult> buildResponse(ErrorHandling error) {
        ApiExceptionEntity entity = ApiExceptionEntity.builder()
                .errorCode(error.getCode())
                .errorMessage(error.getMessage())
                .build();

        return ResponseEntity
                .status(error.getHttpStatus())
                .body(ApiResult.builder()
                        .status("ERROR")
                        .message(error.getMessage())
                        .exception(entity)
                        .build());
    }

}
