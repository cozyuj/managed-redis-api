package com.kakao.api.dto;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import lombok.Data;

@Data
@Group("redis.redis-youjin.com") // CRD group
@Version("v1")                  // CRD version
public class ManagedRedis extends CustomResource<ManagedRedisSpec, ManagedRedisStatus> implements Namespaced {}