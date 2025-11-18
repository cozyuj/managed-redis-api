# Managed Redis API 서버 README

## 1. 프로젝트 개요
이 프로젝트는 Kubernetes 환경에서 관리되는 **Managed Redis 서비스**를 위한 REST API 서버입니다.  
API 서버는 사용자의 요청을 받아 Kubernetes API 서버와 통신하여 **Custom Resource (CR)**를 관리하고, 운영에 필요한 핵심 정보를 제공합니다.  

- 언어: Java (Spring Boot)  
- RDB: 없음 (Kubernetes CR 기반)  
- Kubernetes Client: Fabric8 Kubernetes Client 사용  



## 2. API 목록 및 설명

| HTTP 메서드 | 경로 | 기능 |
|------------|------|------|
| GET | `/v1/clusters` | Redis 클러스터 목록 조회 |
| POST | `/v1/clusters` | Redis 클러스터 생성 |
| GET | `/v1/clusters/{cluster_id}` | 특정 클러스터 상세 조회 |
| PATCH | `/v1/clusters/{cluster_id}` | 클러스터 Replica 수 조정 |
| DELETE | `/v1/clusters/{cluster_id}` | 클러스터 삭제 |

> 필요에 따라 운영 편의성을 위해 Cluster 상태 조회, Replica 정보 조회 API를 추가 구현할 수 있습니다.

### API 설명
1. **클러스터 생성 (POST /v1/clusters)**  
   - 요청 데이터 유효성 검사 후 `ManagedRedis` CR 생성  
   - 초기 Status는 `CREATING`  
   - Controller가 Pod를 생성하고 Phase를 `RUNNING`으로 갱신  

2. **클러스터 조회 (GET /v1/clusters / GET /v1/clusters/{cluster_id})**  
   - CR 상태 및 Pod 상태를 기반으로 Primary/Replica 정보 제공  
   - Service Endpoint(host/port) 포함  
   - JSON 형식으로 응답  

3. **Replica 수 조정 (PATCH /v1/clusters/{cluster_id})**  
   - CR의 `Spec.Replicas` 필드 수정  
   - Controller가 자동으로 Pod 추가/삭제 후 Status 업데이트  

4. **클러스터 삭제 (DELETE /v1/clusters/{cluster_id})**  
   - CR 삭제 요청 → Controller가 Pod/Service 정리 및 Finalizer 제거  



## 3. API 버전 관리 전략
- 모든 엔드포인트는 `/v1`로 버전 관리  
- 향후 Major 버전 변경 시 `/v2` 경로 생성  
- 버전별 호환성을 유지하며 신규 기능 추가 가능  



## 4. Kubernetes API 통신과 RBAC 권한

### 필요 권한
- ManagedRedis CR 리소스: `get`, `list`, `watch`, `create`, `update`, `patch`, `delete`  
- Pod 리소스: `get`, `list`, `create`, `delete` (Controller 관찰용)  
- Service 리소스: `get`, `list`, `create`, `delete`  

> 이유: API 서버는 CR 생성/수정/삭제를 통해 Controller가 Pod/Service를 관리할 수 있도록 해야 합니다.  

### RBAC 예시
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: managedredis-api-role
rules:
- apiGroups: ["redis.yourdomain.com"]
  resources: ["managedredis"]
  verbs: ["get", "list", "watch", "create", "update", "patch", "delete"]
- apiGroups: [""]
  resources: ["pods", "services"]
  verbs: ["get", "list", "create", "delete"]
```

## 5. 성능 및 부하 고려

- **CR 조회 방식**
  - 현재 API 서버는 모든 ManagedRedis CR을 조회한 후 필터링하여 데이터를 반환합니다.
  - 클러스터 수가 많아질 경우 조회 성능이 저하될 수 있습니다.

- **개선 방안**
  - `labelSelector`를 활용하여 클러스터 단위로 조회
  - Namespace 기반 범위 제한
  - 조회 결과 캐싱 적용 가능

- **Replica 스케일링 처리**
  - Pod 생성/삭제는 Controller의 Reconcile 루프에서 수행
  - 멱등성을 보장하여 Reconcile 반복 호출 시 안전하게 처리
  - 재시도 메커니즘(Requeue) 적용으로 안정성 확보

- **부하 분산 고려**
  - 여러 클러스터가 동시에 Scale 요청을 할 경우 Controller에서 순차 처리
  - 필요 시 Queue 또는 Rate Limiter 적용 가능


## 6. 운영 편의 기능 (선택)

- 클러스터 상태 변경 시 알림 Webhook 연동 가능
- Replica Pod 상태 모니터링 및 자동 Failover 기능
- Cluster Endpoint 조회 API 별도 제공 가능
- 필요 시 API 서버에서 상태 캐싱 및 조회 속도 향상
- API 요청 로깅 및 모니터링을 통한 운영 편의성 강화

---

## 7. 사용 예시

### 1) 클러스터 생성
```json
POST /v1/clusters
{
  "name": "redis-demo",
  "namespace": "default",
  "version": "7.0",
  "mode": "primary-replica",
  "replicas": 2
}
```
### 2) 클러스터 상세 조회
```json
GET /v1/clusters/redis-demo
{
  "name": "redis-demo",
  "namespace": "default",
  "version": "7.0",
  "mode": "primary-replica",
  "replicas": 2,
  "status": "RUNNING",
  "primary": {
    "name": "redis-demo-primary-1",
    "podIP": "10.0.0.5",
    "role": "PRIMARY"
  },
  "replicasInfo": [
    {
      "name": "redis-demo-replica-1",
      "podIP": "10.0.0.6",
      "role": "REPLICA"
    }
  ],
  "connection": {
    "host": "redis-demo.default.svc.cluster.local",
    "port": 6379
  }
}
```

### 3) Replica 수 조정
```json
PATCH /v1/clusters/redis-demo
{
  "replicas": 3
}
```

### 4) 클러스터 삭제
```json
DELETE /v1/clusters/redis-demo
```

