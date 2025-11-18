package com.kakao.api.service;

import com.kakao.api.dto.*;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisClusterService {
    private final KubernetesClient kubernetesClient;

    private final Map<String, Map<String, Object>> clusterStatusMap = new ConcurrentHashMap<>();

    public ManagedRedis updateClusterScale(String id, Integer replicas) {
        ManagedRedis redis = kubernetesClient.resources(ManagedRedis.class)
                .inAnyNamespace()
                .list()
                .getItems()
                .stream()
                .filter(cr -> cr.getMetadata().getName().equals(id))
                .findFirst()
                .orElse(null);

        if (redis == null) {
            log.warn("Could not find any cluster scale for id {}", id);
            throw new RuntimeException("Cluster not found: " + id);
        }

        String namespace = redis.getMetadata().getNamespace();

        redis.getSpec().setReplicas(replicas);

        ManagedRedis updated = kubernetesClient.resources(ManagedRedis.class)
                .inNamespace(namespace)
                .withName(id)
                .replace(redis);

        return updated;

    }

    // TODO: 조회 로직 수정
    public ClusterRes getClusterDetail(String uid) {
        log.info("Fetching Redis Cluster by UID: {}", uid);

        // 1. UID 기준으로 CR 검색 (UID 직접 조회는 불가 → 전체 조회 후 필터)
        ManagedRedis target = kubernetesClient.resources(ManagedRedis.class)
                .inAnyNamespace()
                .list()
                .getItems()
                .stream()
                .filter(cr -> cr.getMetadata().getUid().equals(uid))
                .findFirst()
                .orElse(null);

        if (target == null) {
            log.warn("Cluster with UID {} not found", uid);
            return null;
        }

        String namespace = target.getMetadata().getNamespace();
        String name = target.getMetadata().getName();

        ClusterRes res = new ClusterRes();
        res.setName(name);
        res.setNamespace(namespace);
        res.setVersion(target.getSpec().getVersion());
        res.setMode(target.getSpec().getMode());
        res.setReplicas(target.getSpec().getReplicas());
        res.setStatus(target.getStatus() != null ? target.getStatus().getPhase() : "Unknown");

        // 2. Pod 조회 (app: clusterName)
        List<Pod> pods = kubernetesClient.pods()
                .inNamespace(namespace)
                .withLabel("app", name)
                .list()
                .getItems();

        if (!pods.isEmpty()) {
            // 첫 번째 Pod = Primary
            var primaryPod = pods.get(0);
            res.setPrimary(ClusterRes.NodeInfo.builder()
                    .name(primaryPod.getMetadata().getName())
                    .role("PRIMARY")
                    .podIP(primaryPod.getStatus().getPodIP())
                    .status(primaryPod.getStatus().getPhase())
                    .nodeName(primaryPod.getSpec().getNodeName())
                    .build()
            );

            // 나머지 Pod = Replicas
            if (pods.size() > 1) {
                res.setReplicasInfo(
                        pods.subList(1, pods.size())
                                .stream()
                                .map(pod -> ClusterRes.NodeInfo.builder()
                                        .name(pod.getMetadata().getName())
                                        .role("REPLICA")
                                        .podIP(pod.getStatus().getPodIP())
                                        .status(pod.getStatus().getPhase())
                                        .nodeName(pod.getSpec().getNodeName())
                                        .build()
                                )
                                .toList()
                );
            }
        }

        // 3. Service 조회 → ConnectionInfo 세팅
        var svc = kubernetesClient.services()
                .inNamespace(namespace)
                .withName(name)
                .get();

        if (svc != null && svc.getSpec().getPorts() != null && !svc.getSpec().getPorts().isEmpty()) {
            String host = name + "." + namespace + ".svc.cluster.local";
            int port = svc.getSpec().getPorts().get(0).getPort();

            res.setConnection(
                    ClusterRes.ConnectionInfo.builder()
                            .host(host)
                            .port(port)
                            .password(null)
                            .build()
            );
        }

        return res;
    }


    public ManagedRedis createRedisCluster(ClusterReq req){
        log.info("Start creating Redis cluster");
        ManagedRedis cr = new ManagedRedis();

        ObjectMeta meta = new ObjectMeta();
        meta.setName(req.getName());
        meta.setNamespace(req.getNamespace());
        cr.setMetadata(meta);

        // Spec
        ManagedRedisSpec spec = new ManagedRedisSpec();
        spec.setReplicas(req.getReplicas());
        spec.setVersion(req.getVersion());
        spec.setMode(req.getMode());
        cr.setSpec(spec);

        // Status 초기화 (optional, 컨트롤러가 채워줄 수도 있음)
        ManagedRedisStatus status = new ManagedRedisStatus();
        status.setPhase(Status.CREATING.name());
        cr.setStatus(status);

        // 2. Kubernetes에 CR 생성
        ManagedRedis newRedis = kubernetesClient
                .resources(ManagedRedis.class)
                .inNamespace(req.getNamespace())
                .createOrReplace(cr);

        log.info("ManagedRedis CR created: {} in namespace {}", req.getName(), req.getNamespace());

        return newRedis;
    }

    public void deleteCluster(String id) {
        ManagedRedis target = kubernetesClient.resources(ManagedRedis.class)
                .inAnyNamespace()
                .list()
                .getItems()
                .stream()
                .filter(cr -> id.equals(cr.getMetadata().getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not found"));

        String namespace = target.getMetadata().getNamespace();

        ManagedRedis cr = kubernetesClient.resources(ManagedRedis.class)
                .inNamespace(namespace)
                .withName(id)
                .get();

        if (cr == null) {
            log.warn("Deleting non-existing Redis cluster: {}", id);
            throw new ResourceNotFoundException("Cluster name [" + id + "] is not found");
        }

        kubernetesClient.resources(ManagedRedis.class)
                .inNamespace(namespace)
                .withName(id)
                .delete();

    }
}
