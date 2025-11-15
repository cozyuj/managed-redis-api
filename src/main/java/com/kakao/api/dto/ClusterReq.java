package com.kakao.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClusterReq {
    private String name;

    @Builder.Default
    private String namespace = "default";

    @Builder.Default
    private int replicas = 1;

    private String version;

    /** 운영 모드 (예: SINGLE, REPLICATION, CLUSTER) */
    private String mode;

}
