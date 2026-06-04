# Protocol: Sleep-Guard

사용자가 미디어 시청 중 잠들었을 때 이를 지능적으로 감지하여 미디어를 종료하고 배터리를 보호하는 슬립 테크 솔루션입니다.

## 🌟 핵심 컨셉
유튜브, 넷플릭스 등 타겟 미디어 앱 시청 중 수면 상태에 빠지는 것을 센서(가속도)와 UI/UX(Active Ping)로 정밀하게 감지하여 미디어를 자동 정지하고 화면을 꺼줍니다.

## 🎯 주요 기능
- **Targeted App Monitoring:** 사용자가 지정한 특정 앱(유튜브, 넷플릭스 등) 실행 시에만 감지 모니터링 활성화.
- **Foreground Service:** 백그라운드에서도 안정적으로 동작하며 알림창을 통해 즉시 제어 가능.
- **Active Ping (최종 확인):** 수면 의심 시 오버레이 팝업과 진동으로 사용자의 수면 여부를 최종 검증.
- **Media & Screen Killer:** 수면 확정 시 미디어 일시정지 및 화면 강제 잠금.

## 🛠 기술 스택
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Modern Declarative UI)
- **Architecture:** Clean Architecture + MVVM (Flow/Coroutines)
- **Android APIs:** `UsageStatsManager`, `SensorManager`, `DevicePolicyManager`, `MediaSession`

## 📂 프로젝트 구조
```
Sleep-Guard/android/app/src/main/java/com/sleepguard/
├── ui/              # Jetpack Compose UI (Dashboard, Active Ping)
├── sensor/          # Accelerometer 데이터 수집 및 처리
├── service/         # Foreground Service 및 Admin Receiver
├── util/            # 필터 알고리즘, 앱 감지, 미디어 제어 유틸리티
└── MainActivity.kt  # 앱 진입점 및 권한 관리
```
