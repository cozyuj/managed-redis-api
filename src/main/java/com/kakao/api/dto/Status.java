package com.kakao.api.dto;

public enum Status {
    CREATING, //-  Redis 구성 중인 상태
    RUNNING, //- 구성이 완료되어 service의 endpoint까지 노출되는 상태
    SCALING_OUT, //- Replica 노드를 추가하는 경우
    SCALING_IN, //- Replica 노드를 줄이는 경우
    WARNING, // - Replica 노드에 장애가 발생한 경우(복구되면 RUNNING)
    FAILOVER //- Primary 노드에 장애가 발생한 경우(복구되면 RUNNING)
}
