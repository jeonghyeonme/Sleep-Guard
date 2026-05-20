# Protocol: Sleep-Guard

사용자가 미디어 시청 중 잠들었을 때 이를 지능적으로 감지하여 미디어를 종료하고 배터리를 보호하는 슬립 테크 솔루션입니다.

## 🌟 핵심 컨셉
유튜브, 넷플릭스 등 타겟 미디어 앱 시청 중 수면 상태에 빠지는 것을 센서(가속도, 조도)와 UI/UX(Active Ping)로 정밀하게 감지하여 미디어를 자동 정지하고 화면을 꺼줍니다.

## 🎯 주요 기능
- **Targeted App Monitoring:** 사용자가 지정한 특정 앱(유튜브, 틱톡 등) 실행 시에만 감지 모니터링 활성화.
- **3D Context 수면 추론:** 가속도 파형 필터링, 터치 무조작 시간, 시간대 가중치를 결합한 정밀한 수면 판정.
- **Active Ping (최종 확인):** 수면 의심 시 반투명 팝업과 햅틱 진동으로 사용자의 수면 여부를 최종 검증.
- **Media & Screen Killer:** 수면 확정 시 미디어 일시정지 및 화면 Sleep 상태 강제 전환.

## 🛠 기술 스택
- **Framework:** React Native (TypeScript)
- **State/Storage:** AsyncStorage
- **Sensors:** Accelerometer (가속도계), Ambient Light (조도 센서)
- **OS APIs:** `UsageStatsManager` (Android), `MediaSession` (Media Control)

## 📂 프로젝트 구조
```
Sleep-Guard/
├── src/
│   ├── components/  # UI 컴포넌트 (Active Ping 팝업 등)
│   ├── hooks/       # 센서 리스너 및 미디어 제어 커스텀 훅
│   ├── services/    # 포그라운드 서비스 및 네이티브 모듈 브릿징
│   ├── store/       # 설정 및 로그 저장 (AsyncStorage)
│   ├── utils/       # 하이패스 필터링 알고리즘 및 유틸리티
│   └── App.tsx      # 메인 진입점
├── android/         # 네이티브 안드로이드 코드 (Native Modules)
├── assets/          # 아이콘 및 리소스
├── README.md        # 프로젝트 개요
├── PLAN.md          # 상세 설계 및 로드맵
└── GEMINI.md        # 프로젝트 특정 가이드라인
```
