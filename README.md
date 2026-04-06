# ☕ KCF Coffee Shop API

> Spring Boot 기반 커피숍 주문/포인트/결제 백엔드 포트폴리오 프로젝트

<br>

## 📌 프로젝트 개요

다수 인스턴스 환경에서의 **동시성 제어**, **데이터 일관성**, **이벤트 기반 아키텍처**를 직접 구현한 백엔드 포트폴리오 프로젝트입니다.

<br>

## 🛠 기술 스택

| 분류 | 기술 |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 4.0.5 |
| **ORM** | Spring Data JPA, QueryDSL |
| **Database** | MySQL 8.0 |
| **Cache** | Redis (RedisTemplate, Redisson) |
| **Message Queue** | Apache Kafka |
| **Security** | Spring Security, JWT |
| **Test** | JUnit 5, Mockito, H2 |
| **Infra** | Docker, Docker Compose |

<br>

## 🏗 시스템 아키텍처

```
Client
  │
  ▼
Spring Boot API Server
  ├── JWT Authentication (JwtFilter)
  ├── Redis Cache (메뉴 목록)
  ├── Redisson Distributed Lock (주문 동시성)
  ├── Kafka Producer (주문 완료 이벤트)
  └── MySQL (주문 / 결제 / 포인트)

Kafka Broker (KRaft, 3-node)
  └── Kafka Consumer
        └── Redis ZSet (메뉴 인기 랭킹)
```

<br>

## 📂 패키지 구조

```
src/main/java/com/kcfcoffeeshop/
├── common/
│   ├── config/
│   │   ├── security/     # SecurityConfig, JwtFilter, JwtUtil, JwtProperties
│   │   ├── redis/        # RedisConfig
│   │   └── kafka/        # KafkaProducerConfig, KafkaConsumerConfig, KafkaProperties
│   ├── dto/              # BaseResponse<T>, PageResponse<T>
│   └── exception/        # ErrorCode, BusinessException, GlobalExceptionHandler
└── domain/
    ├── user/             # 회원가입, 로그인
    ├── menu/             # 메뉴 목록 조회, 인기 메뉴 조회
    ├── order/            # 주문 생성, Kafka 이벤트
    ├── payment/          # 결제 엔티티
    └── point/            # 포인트 충전, 차감
```

<br>

## 🗂 ERD

### [ERD cloud 바로가기](https://www.erdcloud.com/d/EjZmzpTbo7G2Fe22u)

```
users ──< orders ──< order_items >── menus
              │
              └──< payments

users ──< points ──< point_logs
```

| 테이블 | 설명 |
|---|---|
| `users` | 회원 정보 |
| `menus` | 메뉴 정보 (이름, 가격, 상태) |
| `orders` | 주문 (주문번호, 총금액, 상태) |
| `order_items` | 주문 상세 (메뉴명, 수량, 가격) |
| `payments` | 결제 (결제번호, 금액, 상태) |
| `points` | 포인트 잔액 |
| `point_logs` | 포인트 변동 이력 |

<br>

## 🔑 핵심 기능

### [API 명세서 바로가기](https://documenter.getpostman.com/view/50311221/2sBXiqEojG)


### 1. 회원가입 / 로그인

- BCrypt 비밀번호 암호화
- JWT Access Token / Refresh Token 발급
- Refresh Token Redis 저장 (TTL 7일)

```
POST /api/users/signup
POST /api/users/login
```

---

### 2. 메뉴 목록 조회 with Redis Cache

- Redis 캐싱으로 DB 부하 감소
- Cache Miss: 118ms → Cache Hit: 15ms (약 8배 향상)

```
GET /api/menus?page=0&size=10

Redis Key: "menu:page:{page}:size:{size}" (TTL 1시간)
```

---

### 3. 포인트 충전 with 멱등성

- `Idempotency-Key` 헤더 기반 중복 요청 방지
- 동일 키로 재요청 시 현재 잔액 반환

```
POST /api/points
Header: Idempotency-Key: {UUID}

Redis Key: "idempotency:charge:{key}" (TTL 24시간)
```

---

### 4. 주문 생성 with 분산 락

- **Redisson RLock**으로 다수 인스턴스 환경에서 동시 주문 제어
- 주문번호 / 결제번호 Redis INCR 기반 채번 (형식: `ORD-yyyyMMdd-000001`)
- 단일 트랜잭션 내 주문 → 결제 → 포인트 차감 처리

```
POST /api/orders

Redis Lock Key: "order:lock:{userId}"
tryLock(0, 60, SECONDS)
```

---

### 5. Kafka 이벤트 기반 인기 메뉴 집계

```
주문 완료
  │
  ▼
OrderEventProducer
  │  Topic: order.complete
  │  멱등성 Producer 설정
  ▼
OrderEventConsumer
  │  DLT: order.complete.DLT (FixedBackOff 3회 재시도)
  ▼
MenuRankingService
  └── Redis ZSet 스코어 증가
        Key: "menu:ranking:{year}:W:{weekNum}" (TTL 14일)
```

---

### 6. 인기 메뉴 조회 (TOP 3)

- Redis ZSet reverseRangeWithScores 기반 주간 인기 메뉴 집계

```
GET /api/menus/best
```

<br>

## 🔒 동시성 & 일관성 전략

| 상황 | 해결 방법 |
|---|---|
| 동시 주문 요청 | Redisson 분산 락 (`order:lock:{userId}`) |
| 포인트 중복 충전 | 멱등성 키 (`idempotency:charge:{key}`) |
| Kafka 메시지 중복 처리 | 멱등성 Consumer (`order:processed:{orderNumber}:{menuId}`) |
| 주문번호 중복 채번 | Redis INCR (`order:number:{date}`) |

<br>

## 📊 테스트

```
단위 테스트 (Mockito)
├── UserServiceTest    - 회원가입/로그인 성공 및 예외
├── PointServiceTest   - 충전/차감 성공 및 잔액 부족
└── OrderServiceTest   - 주문 성공/메뉴 없음/락 실패

통합 테스트
└── MenuControllerTest - H2 + Embedded Redis
```

<br>

## 🐳 로컬 실행 방법

### 1. 인프라 실행

```bash
docker-compose up -d
```

| 서비스 | 포트 |
|---|---|
| MySQL | 3306 |
| Redis | 6379 |
| RedisInsight | 5540 |
| Kafka (KRaft, 3-node) | 9092, 9093, 9094 |
| Kafka UI | 8088 |

### 2. Kafka 토픽 생성

```bash
# 파티션 3개로 토픽 생성
docker exec -it kafka-1 kafka-topics.sh \
  --create \
  --topic order.complete \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 3

docker exec -it kafka-1 kafka-topics.sh \
  --create \
  --topic order.complete.DLT \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 3
```

### 3. 환경 변수 설정

`src/main/resources/application-local.yaml` 생성 (`.gitignore` 적용)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kcf_coffee_shop_db
    username: {YOUR_USERNAME}
    password: {YOUR_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: create-drop
  data:
    redis:
      host: localhost
      port: 6379

kafka:
  bootstrap-servers: localhost:9092,localhost:9093,localhost:9094

jwt:
  secret: {YOUR_JWT_SECRET}
```

### 4. 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

<br>

## 📁 브랜치 전략

| 브랜치 | 설명 |
|---|---|
| `main` | 최종 배포 브랜치 |
| `dev` | 개발 통합 브랜치 |
| `feature/user-signup` | 회원가입 |
| `feature/user-login` | 로그인 |
| `feature/menu-list` | 메뉴 목록 조회 + Redis 캐시 |
| `feature/point-charge` | 포인트 충전 |
| `feature/point-charge-idempotency` | 포인트 멱등성 |
| `feature/order-create` | 주문 생성 |
| `feature/redisson` | 분산 락 적용 |
| `feature/kafka-order-event` | Kafka 주문 이벤트 |
| `feature/menu-best` | 인기 메뉴 조회 |
| `feature/test` | 통합 테스트 |