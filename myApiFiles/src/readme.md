1. 프로젝트 구성안내
2. 프로젝트 설치 안내
3. 프로젝트 구동 안내
4. 프로젝트 사용법
5. 프로젝트 기능설명
6. 저작권 및 사용자 정보
7. 버그
8. 프로그램 작성자 및 도움을 준 사람
9. 버전 (업데이트 소식)
10. FAQ

## File upload

### 환경설정
- 환경구성
    - mysql 설치
    - 스키마 생성 (file-apis)
    - application.properties mysql 정보 수정 (본인 환경에 맞도록)

#### 파일저장: path YYYY/MM/DD/HH

#### 파일명: hash (MD5) 저장

    1. 파일을 업로드 한다.
    2. 파일을 로컬에 복사 한다.
    3. 복사된 파일로 채크썸으로 파일명을 만든다. 
    4. 채크썸으로 복사된 파일을 리네임 하여 저장한다.


        => 체크섬을 체크섬으로 리네임 하면 체크썸이 유지가 되나?? 
        => 체크썸으로 리네임 한다는 것은 리네임된 체크썸으로 저장하겠다느 뜻이 아님
        => 그냥 그렇게 원본을 MD5 저장 하겠단 뜻임
    5. 파일 요청 퍼미션 요청사항에 따라 저장 위치를 달리 한다.
        => 전체 공계 파일 file.public-space-dir
        => 보호된 파일 file.protect-space-dir
    6. 파일 유형을 분석해서 경로 분류
        => https://myhappyman.tistory.com/133


### 파일업로그 기능 목록

    1. 파일업로드 기능
    2. 파일복사 기능 
    3. 경로분리 기능 공개, 비공계
    4. 경로작성 기능  ~/filePaht/YYYY/MM/DD/HH
    5. 파일읽어 http, https 로 내려주기 
    6. 파일을 공유 한다.
    7. 공동 열람자를 지정한다.

#### 파일 그룹 관련
   1. 파일은 공동 사용자에 대한 키에 대해서 해쉬를 작성 하고 그 해쉬는 로그인한 사용자만 생성된다.
   2. 해쉬를 가지고 특정 파일열람 요청이 오면 해쉬 (기관코드, 계약번호, 사용자코드의 합 등) 여부를 확인 해서 해쉬가 있을 경우 열람이 가능하도록 한다.
   3. 열람에 대한 기준은 해당 파일의 그룹 정책에 따른다. 
   4. 그룹정책이 PUBLIC 일 졍우 해당 해쉬는 대한 부분은 판단하지 않는다.  

####

~.hwp =>  application/x-tika-msoffice
~.xlsx => application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
~.pptx => application/vnd.openxmlformats-officedocument.presentationml.presentation
~.docx => application/vnd.openxmlformats-officedocument.wordprocessingml.document
~.doc =>  application/x-tika-msoffice
~.xls => application/x-tika-msoffice
~.json => text/plain

jellyfish-4925772__340.jpg => image/jpeg 2021-01-14 08;40;18.PNG => image/png

r(Read) : 4 w(Write) : 2 x(Execute) : 1 r + w + x : 7

#### 확장자: file 로 통일 (public 파일 제외)

#### 원본파일 정보는 DB에 별도 저장

#### 말단 디렉터리에 파일 정보 저장 : 원본파일.확장자 => MD5.file

#### 권한정의

    - 소유자 
    - 읽기, 쓰기

#### 파일일 특정 태그 일때 특정 서버 경로로 저장 가능

- ConfigurationProperties
    - https://sgc109.github.io/2020/07/07/spring-boot-configuration-properties/
- spring boot json 과 파일 동시 처리 관련 포스트 :
    - https://ykh6242.tistory.com/115
    - https://blogs.perficient.com/2020/07/27/requestbody-and-multipart-on-spring-boot/
        - test case는 되나 post man으로는 처리 되지 않음.
- 사용자 파일 저장 프로퍼티 값

```
file.upload-dir= ${user.home}/Download
file.public-space-dir= ${user.home}/Download
file.protect-space-dir= D${user.home}/Download
```

#### 전문포멧

```
    {
        파일명 :"e95d7235-8b58-43a4-8e00-bd603b6a3f0a.txt",
        다운로드 경로 : "http://localhost/downloadFile/e95d7235-8b58-43a4-8e00-bd603b6a3f0a.txt",
        파일 타입 :"text/plain",
        원본파일명 : "hello.txt",
        해쉬값:"65a8e27d8879283831b664bd8b7f0ad4",
        크기: 13,
        파일 권한: ["OWNER_WRITE","OWNER_READ"]
        파일 액세스 가능자: [{"idAccessCode":"$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju"},{"idAccessCode":"f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7"},{"idAccessCode":"fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a"}]
        작성일: 
        소유자:  
    }
    {
        "fileName":"e95d7235-8b58-43a4-8e00-bd603b6a3f0a.txt",
        "fileDownloadUri":"http://localhost/downloadFile/e95d7235-8b58-43a4-8e00-bd603b6a3f0a.txt",
        "fileType":"text/plain",
        "originFileName":"hello.txt",
        "hashName":"65a8e27d8879283831b664bd8b7f0ad4",
        "size":13,
        "filePermissions":["OWNER_WRITE","OWNER_READ"],
        "filePermissionGroups":[
            {"idAccessCode":"$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju"},
            {"idAccessCode":"f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7"},
            {"idAccessCode":"fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a"}]
     }
```

### 테스트 방법
#### Junit Test Case
- FilControllerTest 실행

#### web ui
- http://localhost:8080/demo-file-upload.html 호출 후 해당 필드 입력하여 전송
    - 참고 : Server 측 셋팅 추가 
```
Access-Control-Allow-Orgin : 요청을 보내는 페이지의 출처 (*, 도메인)
Access-Control-Allow-Methods : 요청을 허용하는 메소드 (Default : GET, POST, HEAD)
Access-Control-Max-Age : 클라이언트에서 pre-flight의 요청 결과를 저장할 시간 지정. 해당 시간 동안은 pre-flight를 다시 요청하지 않는다.
Access-Control-Allow-Headers : 요청을 허용하는 헤더
```

#### Post Man

- POST 요청
- url 입력 localhost:8080/upload-file-permission-json-file
- body
    - form-data
    - key : file, metadata
    - value : 
        - file: 아무파일이나 
        - metadata: [project] > test > resources > data > permission.json 파일 선택