# GlideCam - Minecraft 시네마틱 카메라 모드

마인크래프트에서 전문적인 시네마틱 영상을 제작할 수 있는 카메라 모드입니다.

## 주요 기능

- ✅ **부드러운 곡선 이동**: Catmull-Rom 스플라인을 사용한 부드러운 카메라 경로
- ✅ **엔티티 추적**: 특정 엔티티를 자동으로 따라가고 바라보기
- ✅ **고정 좌표 바라보기**: 카메라가 이동하면서도 특정 좌표를 계속 바라봄
- ✅ **Waypoint 시스템**: 카메라 위치를 저장하고 관리
- ✅ **경로 재생**: 저장된 경로를 따라 자동 재생
- ✅ **FOV 조정**: 시야각 조정으로 다양한 시네마틱 효과
- ✅ **서버 호환**: 서버와 클라이언트 모두 지원

## 설치 방법

1. Minecraft 1.20.1과 Forge 47.3.0 이상 설치
2. 모드 파일을 `mods` 폴더에 복사
3. 게임 실행

## 빌드 방법

```bash
# Windows
.\gradlew.bat build

# Linux/Mac
./gradlew build
```

빌드된 파일: `build/libs/GlideCam-1.20.1-1.0.1.jar`

## 명령어 사용법

### 기본 제어

```
/camera start              # 시네마틱 모드 시작
/camera stop               # 시네마틱 모드 중단
/camera goto <x> <y> <z>   # 특정 위치로 이동
/camera rotate <pitch> <yaw>  # 카메라 회전
/camera help               # 도움말 표시
```

### Waypoint 관리

```
/camera waypoint add <name>                    # 현재 위치에 Waypoint 추가
/camera waypoint add <name> <x> <y> <z> <pitch> <yaw>  # 수동으로 Waypoint 추가
/camera waypoint remove <name>                 # Waypoint 삭제
/camera waypoint list                          # 모든 Waypoint 목록
/camera waypoint goto <name>                   # Waypoint로 이동
```

### 경로 관리

```
/camera path create <name>                     # 새 경로 생성
/camera path add <path> <waypoint>             # 경로에 Waypoint 추가
/camera path insert <path> <index> <waypoint>  # 특정 위치에 Waypoint 삽입
/camera path remove <path> <index>             # 경로에서 Waypoint 제거
/camera path list                              # 모든 경로 목록
/camera path show <name>                       # 경로 상세 정보
/camera path delete <name>                     # 경로 삭제
/camera path speed <path> <speed>              # 경로 재생 속도 설정 (0.1~10.0)
```

### 재생 제어

```
/camera play <path>        # 경로 재생
/camera play <path> loop   # 경로 반복 재생
/camera pause              # 재생 일시정지
/camera resume             # 재생 재개
```

### 엔티티 추적

```
/camera track <entity>                    # 엔티티 바라보기
/camera track follow <entity> <distance>  # 엔티티 따라가기
/camera track stop                        # 추적 중단
```

### 고정 좌표 바라보기

```
/camera lookat <x> <y> <z>  # 특정 좌표 바라보기
/camera lookat stop         # 바라보기 중단
```

### 카메라 설정

```
/camera fov <value>        # FOV 설정 (30~110)
/camera smooth <value>     # 부드러움 설정 (0.0~1.0)
/camera roll <angle>       # 카메라 롤 설정
/camera info               # 현재 카메라 정보
```

## 사용 예시

### 1. 기본 경로 만들기

```
# Waypoint 추가
/camera waypoint add start
/tp 100 64 100
/camera waypoint add middle
/tp 200 80 200
/camera waypoint add end

# 경로 생성 및 Waypoint 추가
/camera path create my_path
/camera path add my_path start
/camera path add my_path middle
/camera path add my_path end

# 재생
/camera play my_path
```

### 2. 엔티티 추적하면서 경로 이동

```
# 경로 재생과 동시에 엔티티 추적
/camera play my_path
/camera track @e[type=minecraft:cow,limit=1]
```

### 3. 특정 건물을 바라보면서 원형 경로

```
# 중심 좌표 설정
/camera lookat 100 70 100

# 원형으로 Waypoint 배치 후 경로 재생
/camera play circle_path
```

## 기술 사양

- **Minecraft 버전**: 1.20.1
- **Forge 버전**: 47.3.0+
- **Java 버전**: 17
- **보간 알고리즘**: Catmull-Rom Spline
- **업데이트 주기**: 60 FPS (매 틱)
- **서버/클라이언트**: 양쪽 모두 지원

## 라이선스

MIT License

## 기여

버그 리포트나 기능 제안은 GitHub Issues를 통해 제출해주세요.

## 크레딧

- Catmull-Rom 스플라인 보간
- Minecraft Forge API
- 커뮤니티 피드백

---

**즐거운 영상 제작 되세요! 🎬**
