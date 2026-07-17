# Mobile Access Control — 휴대형 NFC 출입검문 단말 앱

사원증(NFC 카드)을 휴대 단말로 스캔해 출입 허용/거부를 판정하는 **검문 단말 앱**입니다.
Android 클라이언트(본 저장소)와 Spring Boot API 서버(별도 저장소)로 구성된 풀스택 개인 프로젝트입니다.

> 서버 저장소: [access-terminal-server](https://github.com/GEUN-TAE-KIM/access-terminal-server) — Spring Boot (Kotlin) + PostgreSQL 16

---

## 핵심 설계 포인트

### 1. 오프라인에도 검문이 동작하는 Hybrid 구조 (store-and-forward)

검문 단말의 1순위 책임을 **검문 가용성**으로 정의했습니다. 신용카드 POS 단말이 오프라인에서도
store-and-forward 로 결제를 진행하듯, 네트워크가 끊겨도 검문은 계속됩니다.

- **온라인**: `POST /access/verify` 로 서버 판정
- **오프라인**: Room 에 캐시된 master snapshot (`GET /master/snapshot`, ETag/304 conditional GET) 으로
  단말이 자체 판정 (`LocalAccessVerifier`) + audit 로그 큐잉 → 네트워크 복구 시 WorkManager 가 일괄 전송
- 클라이언트/서버 판정기가 같은 결과를 내는지는 **양쪽 repo 가 공유하는 golden test JSON** (11 케이스) 으로 CI 검증

### 2. 18-모듈 멀티모듈 아키텍처 — 가시성으로 강제하는 의존 규칙

`:app → :feature:* → :component:* → :core:*` 단방향. `:component:*` 의 `data/` 구현은 전부
`internal` 로 은닉되어 `:feature` 가 domain 계약(interface·model)만 볼 수 있습니다 — 모듈 분리의
목적을 "빌드 속도"가 아니라 **은닉화 + 의존 제어**에 두었습니다. cross-component 의존은
화이트리스트 7쌍으로 제한합니다.

### 3. NFC 저수준 통제 — Reader Mode + Strategy

- `NfcAdapter.enableReaderMode` 만 사용 (Foreground Dispatch 의도적 배제) — Compose `DisposableEffect` 와
  화면 생명주기 1:1, 시스템 효과음/햅틱 차단 (`FLAG_READER_NO_PLATFORM_SOUNDS`)
- FeliCa / IsoDep 두 reader 를 Hilt multibinding 전략 패턴으로 등록, UID/IDm 만 추출
- **NDEF 의도적 제외** — 평문·무인증이라 사원증 검증에 부적합 (5초면 복제 가능)

### 4. 타입 안전 에러 모델 — `Outcome<T, E : AppError>`

외부 함수형 라이브러리 없이 Kotlin 표준 sealed + `when` exhaustiveness 만으로 전 도메인 결과를
컴파일 타임 강제합니다. 도메인별 sealed 에러 7종 (`AuthError`/`AccessError`/`CardError`/`NfcError`/
`HistoryError`/`StatsError`/`MasterError`), `else` 분기 금지, Throwable 은 data 레이어 진입점에서 봉인.

### 5. 클라이언트 친화적 API 계약 (서버와 공동 설계)

- 출입 거부(DENIED)는 4xx 가 아니라 `200 OK` + `result` enum — 클라 try/catch 분기 제거
- RFC 7807 Problem Details + `errorCode`/`retryable` 확장
- Idempotency-Key 재사용 (한 검문 = 한 키), Refresh Token Rotation + `Mutex` single-flight refresh
- Cursor pagination (composite ts+id, opaque Base64), replay 방어 (nonce + timestamp ±5분)

---

## 화면 구성

| 화면 | 설명 |
|---|---|
| 로그인 | 운영자 인증. 로그인 직후 master sync 를 await 해 오프라인 검문 가용성 확보 |
| 검문 (Scan) | zone 선택 → 카드 스캔 → 허용/거부 판정 표시. Debug 빌드에선 Mock 카드 패널 제공 |
| 카드 등록 | ADMIN 이 미등록 카드를 사번(employeeCode)에 매핑 |
| 기록 (History) | 출입 로그 cursor pagination 조회 + 서버측 필터 (결과/사유/사번/카드/기간) |
| 통계 (Stats) | 기간별 출입 통계 |

시연은 Debug 빌드의 Mock 패널로 실물 카드 없이 가능합니다 — `EMP001`(허용), `EMP002`(권한 없음),
`EMP003`(퇴사자), `EMP004`(시간대 제한), `EMP005`(분실 카드) 5개 시나리오가 서버 sample data 와
매핑되어 있습니다.

---

## 기술 스택

| 영역 | 선택 |
|---|---|
| UI | Jetpack Compose (BOM 2026.05), Material3, Navigation Compose 2.9 (type-safe) |
| 아키텍처 | 18-모듈, MVI (Orbit 11), single-activity |
| DI | Hilt 2.59 (+ HiltWorker) |
| 비동기 | Coroutines 1.11 |
| 네트워크 | Retrofit 3, OkHttp 5, kotlinx-serialization |
| 영속화 | Room 2.8, DataStore 1.2 (+ AndroidKeyStore AES-256-GCM 토큰 암호화) |
| 백그라운드 | WorkManager 2.11 |
| 테스트 | JUnit4, MockK, orbit-test, kotlinx-coroutines-test |
| 빌드 | Gradle 9.4, AGP 9.2, Kotlin 2.3, KSP / JDK 11, minSdk 26, targetSdk 36 |

---

## 모듈 구조

```
:app                  컴포지션 루트 (Hilt, NavGraph, 횡단 @Module)
:feature:*            화면 (login / scan / register / history / stats / common)
:component:*          비즈니스 (auth / access / scan / nfc / master / sync / history / stats)
:core:network         Retrofit·OkHttp factory + Interceptor (pure JVM)
:core:database        Room (AccessDatabase + DAO)
:core                 TimeProvider · Outcome · value class · qualifier (pure JVM)
```

---

## 빌드·실행

```bash
./gradlew :app:assembleDebug        # Debug APK
./gradlew :app:installDebug         # 연결된 단말에 설치
./gradlew test                      # 전체 유닛 테스트
```

- 요구사항: JDK 11+, Android SDK 36
- `BuildConfig.BASE_URL` / `TERMINAL_ID` 는 `app/build.gradle.kts` 에서 설정
- 서버 없이 체험: Debug 빌드 → 검문 화면 Mock 패널 (오프라인 판정 경로로 동작)

---

## 문서

클라이언트 설계 결정 근거는 각 모듈의 코드와 KDoc 주석에 인라인으로 남겨져 있습니다.

API wire 명세의 단일 출처는 서버 저장소 [`access-terminal-server`](https://github.com/GEUN-TAE-KIM/access-terminal-server) 의 `docs/api-spec.md` 입니다.
