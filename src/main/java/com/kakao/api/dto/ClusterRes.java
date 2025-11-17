package com.kakao.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterRes {
    private String name;
    private String namespace;
    private String version;

    /** 복제 모드: SINGLE, REPLICATION, CLUSTER */
    private String mode;

    private int replicas;

    /** 클러스터 상태: Pending, Running, Failed, Scaling 등 */
    private String status;

    /** Primary 노드 정보 */
    private NodeInfo primary;

    /** Replica 노드 목록 */
    private List<NodeInfo> replicasInfo;

    /** Redis 접속 정보 (Service, Port, Password 등) */
    private ConnectionInfo connection;

    @Data
    @Builder
    public static class NodeInfo {
        private String name;
        private String role; // PRIMARY or REPLICA
        private String podIP;
        private String status; // Running, Pending, Failed 등
        private String nodeName; // 배포된 노드
    }

    @Data
    @Builder
    public static class ConnectionInfo {
        private String host;
        private int port;
        private String password; // optional, masked or encrypted
    }
}
