## 프로젝트 구조

###myApiFiles

  - 파일옹 api
  - 호출 인자값
    - 업로드
      - 파일 (byte)
      - 사용자 코드 (email, 등 id 값, hash 후 보내면 추가 해쉬 진행)
      - 도메인 정보 (www.abc.com 등 자연어 값 hash 후 보내면 추가 해쉬 진행)
    - 다운로드 

###myApiMembers
    - 기본 사용자
###myApiPosts
    - 기본 게시판
###myHash
    - 사용자정보 해쉬용 참조 보듈
    - api를 활용하는 클라이언트 삽입용.
    - jdk 별로 컴파일 필요.
