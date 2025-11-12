package com.kakao.api.exception;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public enum ErrorHandling {
    // 시스템 에러 메시지
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-500", "서버 내부 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "SYS-400", "요청 데이터가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "SYS-401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "SYS-403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "SYS-404", "요청한 리소스를 찾을 수 없습니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "SYS-429", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),


    // 클러스터 및 레디스 관련 에러 메시지
    CLUSTER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CLUSTER-001", "클러스터 생성에 실패했습니다."),
    CLUSTER_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUSTER-002", "해당 클러스터를 찾을 수 없습니다."),
    CLUSTER_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CLUSTER-003", "클러스터 업데이트 중 오류가 발생했습니다."),
    CLUSTER_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CLUSTER-004", "클러스터 삭제 중 오류가 발생했습니다."),

    REDIS_DEPLOY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS-001", "Redis 리소스 배포에 실패했습니다."),
    REDIS_NOT_READY(HttpStatus.BAD_REQUEST, "REDIS-002", "Redis 클러스터가 준비되지 않았습니다."),
    REDIS_VERSION_UNSUPPORTED(HttpStatus.BAD_REQUEST, "REDIS-003", "지원하지 않는 Redis 버전입니다."),


    // 네트워크 관련 에러 메시지
    K8S_API_COMMUNICATION_FAILED(HttpStatus.BAD_GATEWAY, "K8S-001", "Kubernetes API 서버와 통신에 실패했습니다."),
    K8S_RESOURCE_CONFLICT(HttpStatus.CONFLICT, "K8S-002", "이미 존재하는 Kubernetes 리소스입니다."),
    K8S_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "K8S-003", "Kubernetes 접근 권한이 없습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private String message;

    ErrorHandling(HttpStatus httpStatus, String code) {
        this.httpStatus = httpStatus;
        this.code = code;
    }

    ErrorHandling(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
