package com.kakao.api.controller;

import com.kakao.api.dto.ClusterReq;
import com.kakao.api.dto.ClusterRes;
import com.kakao.api.dto.ClusterScaleReq;
import com.kakao.api.dto.ManagedRedis;
import com.kakao.api.service.RedisClusterService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/clusters")
public class ClusterController {

    private final RedisClusterService redisService;

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateClusterScale(@PathVariable String id,
                                           @RequestBody ClusterScaleReq req){
        try {
            ManagedRedis updated = redisService.updateClusterScale(id, req.getReplicas());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createCluster(@RequestBody ClusterReq req){
        try {
            ManagedRedis redis = redisService.createRedisCluster(req);
            return ResponseEntity.ok(redis);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCluster(@PathVariable String id){
        ClusterRes res = redisService.getClusterDetail(id);
        return ResponseEntity.ok(res);
    }
}
