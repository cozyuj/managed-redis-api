package com.kakao.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManagedRedisStatus {
    private String phase;       // CREATING, RUNNING, etc.
    private String endpoint;    // Redis 접속 endpoint
    private int replicas;

    @Builder
    public ManagedRedisStatus(String phase, String endpoint, int replicas) {
        this.phase = phase;
        this.endpoint = endpoint;
        this.replicas = replicas;
    }
}
