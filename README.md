# 2024 데이터베이스기초 203팀 프로젝트 과제

## 개발 환경
- Java 17
- MySQL
- Maven
- IntelliJ IDEA


### 2. JDBC 연결 설정
1. `src/main/resources` 폴더에 `.env` 파일 생성
2. 아래 내용을 본인의 DB 정보에 맞게 수정

```
DB_URL=jdbc:mysql://localhost:3306/~~
DB_USER=your_username
DB_PASSWORD=your_password
```

### 브랜치 구조
- `main`: 최종 배포용 브랜치
- `feature/`: 기능 개발 브랜치(아래는 예시)
    - `feature/employee-list`
    - `feature/search-function`


### Pull Request
1. GitHub에서 Pull Request 생성
2. 코드 리뷰 진행
3. 승인 후 main 브랜치에 병합

## 커밋 메시지 컨벤션
- feat: 새로운 기능 추가
- fix: 버그 수정
- docs: 문서 수정
- style: 코드 포맷팅, 세미콜론 누락 등
- refactor: 코드 리팩토링
- test: 테스트 코드
- chore: 빌드 업무 수정, 패키지 매니저 수정

- 예시:
```
  
  git commit -m "feat: Add employee search function"
  git commit -m "fix: Fix database connection error"
  git commit -m "docs: Update README.md"
```

## 주의사항
- `.env` 파일은 절대 git에 커밋하지 않기(gitignore에 추가 되어 있음)
- 항상 최신 main 브랜치에서 새로운 feature 브랜치를 생성
- PR 생성 전에 코드 충돌이 없는지 확인