# 📖 BookLog (북로그) - 나만의 독서 기록 & 도서 검색 앱

![Android](https://img.shields.io/badge/Android-35D483?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Room](https://img.shields.io/badge/Room_DB-4285F4?style=for-the-badge&logo=sqlite&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit2-000000?style=for-the-badge)

> **BookLog**는 읽은 책의 감상과 추억을 기록하고, 내가 언제 어디서 책을 읽었는지 지도와 타임라인으로 한눈에 돌아볼 수 있는 안드로이드 독서 로그 애플리케이션입니다.

---

## 📱 프로젝트 개요 (Overview)

* **앱 명칭**: BookLog (북로그)
* **주요 목적**: 알라딘 도서 API 연동을 통한 실시간 책 검색, 독후감 기록 작성, 위치 기반 지도 마커 저장, 연/월별 서가(그리드) 및 타임라인 독후감 관리
* **디자인 컨셉**: 종이 질감의 차분한 세이지 카키 & 베이지 테마 (`#526B52` / `#F7F5EE`)

---

## ✨ 주요 기능 (Key Features)

### 1. 🏠 메인 화면 (Home)
* **베스트셀러 TOP 10**: 알라딘 실시간 베스트셀러 도서 10권을 큐레이션하여 제공
* **빠른 이동**: 상단 메인 카드를 통해 도서 검색 및 라이브러리 화면으로 원터치 이동
* **상단 바 자동 최적화**: 홈 화면 진입 시 헤더 바를 깔끔하게 숨기고 종이 질감 배경과 어우러지는 상태바 스타일 제공

### 2. 🔍 도서 검색 (Search)
* **실시간 도서 검색**: 알라딘 `ItemSearch` API를 통해 제목 및 저자 키워드로 실시간 책 검색 (페이징 지원)
* **검색 결과 및 상세 연동**: 책 이미지, 제목, 저자 정보 확인 후 클릭 한 번으로 독후감 작성 페이지 이동
* **직관적인 검색 상태**: 검색 결과가 없을 때나 초기 진입 시 안내 일러스트와 검색 유도 문구 표시

### 3. 📝 독후감 작성 (Review Write)
* **도서 정보 자동 입력**: 선택한 도서의 제목, 저자, 표지 URL 자동 로드
* **위치 정보 자동 저장**: GPS/네트워크 위치 기반으로 독후감을 작성한 장소의 위도/경도 자동 태깅
* **다채로운 기록 기능**: 5점 단위 별점 평가, 갤러리 이미지/사진 첨부, 텍스트 감상 노트 작성
* **자동 임시저장**: 작성 중 화면 전환이나 앱 종료 시에도 입력 중인 내용 보존
* **저장 후 연동**: 완료 시 즉시 저장 안내 팝업과 함께 상세 독후감 화면으로 전환

### 4. 📚 라이브러리 (Library - Grid View)
* **3열 서가 그리드**: `RecyclerView` `GridLayoutManager`를 활용하여 서점 서가처럼 책 표지 모아보기
* **연/월별 그룹화**: 작성 날짜 기준 내림차순(최신순)으로 연도 및 월(예: `2026년 9월`) 헤더 구분
* **상세 보기**: 표지 터치 시 해당 독후감 상세 페이지로 즉시 이동

### 5. ⏱️ 타임라인 (Timeline - List View)
* **연/월별 내림차순 타임라인**: 날짜 순서대로 독후감을 시간 순으로 감상할 수 있는 타임라인 피드
* **상세 요약 정보**: 책 표지, 제목, 저자, 별점, 작성 장소 태그, 감상문 요약본 한눈에 확인

### 6. 🗺️ 지도 탭 (Map View)
* **구글 지도 연동**: `Google Maps SDK` 기반으로 사용자가 어디서 책을 읽고 독후감을 남겼는지 마커 표시
* **마커 터치 연동**: 지도 위의 독후감 마커를 터치하면 해당 독후감 상세 화면으로 이동

### 7. 📖 독후감 상세 (Review Detail)
* **독후감 정보 조회**: 책 표지/첨부 사진, 제목, 저자, 별점, 작성 날짜, 작성 장소 및 감상문 전문 확인
* **수정 및 삭제**: 기존 독후감 수정 및 삭제 기능 제공

---

## 🎨 UI & Design Aesthetic

* **Main Theme Color**: Sage Khaki Green (`#526B52`) - 시각적으로 편안하고 감성적인 서점 스타일
* **Background Color**: Soft Paper Beige (`#F7F5EE`) - 종이 책의 느낌을 주는 따뜻한 아날로그 배경
* **Surface Card**: Warm White (`#FFFFFF`) - 높은 가독성의 카드 레이아웃
* **Navigation & Top Header**:
  * 각 화면별 명확한 제목 표시 및 `<` 뒤로가기 버튼 지원
  * 하단 5개 탭 네비게이션 바 (**홈 / 검색 / 지도 / 라이브러리 / 타임라인**)
  * 동일 탭 재터치 시 해당 탭의 최상위 초기 화면으로 즉시 복귀하는 UX 처리

---

## 🛠 기술 스택 (Tech Stack)

| 구분 | 기술 스택 / 라이브러리 |
| :--- | :--- |
| **Language** | Kotlin 1.9+ |
| **Architecture** | Single Activity Multi-Fragment (MVVM, Repository Pattern) |
| **UI Components** | Jetpack Navigation Component, ViewBinding, ViewModel, SavedStateHandle, Material Components |
| **Database** | Room Persistence Library (`AppDatabase`, Room Migration 2→3→4 지원) |
| **Network & API** | Retrofit 2, Gson Converter, OkHttp 3 |
| **Image Loading** | Glide v4 (Aladin CDN Custom User-Agent Header 적용) |
| **Location & Map** | Google Maps SDK (`play-services-maps`), Fused Location Provider (`play-services-location`) |
| **Concurrency** | Kotlin Coroutines, StateFlow / SharedFlow |

---

## 🔌 사용 OpenAPI (Aladin Open API)

* **상품 검색 API (ItemSearch)**: 키워드로 알라딘 도서 목록 검색
  `http://www.aladin.co.kr/ttb/api/ItemSearch.aspx`
* **상품 리스트 API (ItemList)**: 실시간 베스트셀러 도서 목록 획득
  `http://www.aladin.co.kr/ttb/api/ItemList.aspx`
* **상품 조회 API (ItemLookUp)**: ISBN13 기준 도서 상세 정보 및 고화질 표지 조회
  `http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx`

---

## 📂 프로젝트 구조 (Directory Structure)

```text
com.example.booklog
├── MainActivity.kt                  # 메인 액티비티 (TopBar, BottomNav, WindowInsets)
├── data
│   ├── aladin                       # 알라딘 Open API (Retrofit, Models, Client)
│   ├── local                        # Room DB (AppDatabase, ReviewDao, ReviewEntity, Migrations)
│   ├── location                     # 위치 정보 (ReviewLocationProvider)
│   └── repository                   # 데이터 저장소 (BookRepository, ReviewRepository)
└── ui
    ├── home                         # 메인 홈 화면 (베스트셀러 TOP 10)
    ├── search                       # 도서 검색 화면
    ├── review                       # 독후감 작성 (ReviewWrite) 및 상세 (ReviewDetail)
    ├── library                      # 라이브러리 (그리드 서가 뷰)
    ├── timeline                     # 타임라인 (연/월별 피드 뷰)
    ├── map                          # 지도 화면 (위치 마커 뷰)
    └── utils                        # 공통 유틸리티 (ImageUtils Glide 로더)
```

---

## ⚙️ 실행 및 API 키 설정 안내

이 프로젝트를 실행하려면 `local.properties` 파일에 알라딘 TTB 키 및 구글 지도 API 키 설정이 필요합니다.

```properties
# local.properties 예시
sdk.dir=C\:\\Users\\username\\AppData\\Local\\Android\\Sdk
ALADIN_TTB_KEY=your_aladin_ttb_key_here
GOOGLE_MAPS_API_KEY=your_google_maps_api_key_here
```

---

© 2026 BookLog Team. All rights reserved.
