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

### 명세
- 명명규칙 
  - url : 
    - 명사 - 동사 ex) /file-download
    - dash(-) 를 사용하여 단어와 단어 구분 
  - method : 동사 - 명사 ex) downloadFile()
    - 카멜(Camel)을 사용하여 표시
> url 과 클래스명  java 에서는 각각 제약사항과 표기에 대한 해석이 다르기 때문이다.

- 에러 정책
  - 4XX : 사용자의 잘못된 요청
    - 요청 값 중 필수 값 누락시 제공
 

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


### 실행방법

#### 개발환경

#### 운영환경
- jar 로 기동 (기본) : 기존 설정대로 사용한다.
- jar 로 기동 (확장) : 각 하드웨어 셋팅에 맞추어 기본적인 속성을 overwrite 한다.
  - 참고 URL : 
    - https://www.baeldung.com/spring-properties-file-outside-jar
    - http://honeymon.io/tech/2021/01/16/spring-boot-config-data-migration.html
 
- 필수 교체 속성:
  - DB 관련 속성 변경
  - 파일 저장패스 변경
  - 파일 다운로드 관련 uri 변경
- 환경변수를 담은 실행 shell 을 만들서어 사용한다.
- 

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

#### 이미지 리사이징
JVM 힙 크기를 조정 산식 (대략적임)
1. 이미지 로딩 가로 해상도 X 세로해상도 X 4 = byte
2. 이미지 변환 해상도 = 이미지 로딩 해상도 
3. 총 필요 메모리 = 로딩 해상도 + 변환 해상도
   - 15000 x 15000 x 4 = 900M
   - 900M + 900M = 1800M 
   - 총 필요 heap memory 1800M
   - 
   
> -Dthumbnailator.conserveMemoryWorkaround=trueJVM 옵션 을 추가하여 활성화할 수 있는 필요한 메모리를 줄이기 위한 임시 해결 방법(항상 작동하지 않을 수 있음 )이 있지만 최종 썸네일 품질이 저하될 수 있습니다.
> at com.sun.imageio.plugins.jpeg.JPEGImageReader.readInternal(JPEGImageReader.java:1082)
> at com.sun.imageio.plugins.jpeg.JPEGImageReader.read(JPEGImageReader.java:1050)



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

### 전문포멧
#### 파일정보요청
```json
{"filePhyName":"95b02285-a79a-4b7e-944d-c53d73b8907d.txt","ownerDomainCode":"19103e6354655886cb2f46880a4ae116","ownerAuthenticationCode":"41a11f24348d2c513c5f0acac52d3531","fileCheckSum":"65a8e27d8879283831b664bd8b7f0ad4"}
```
#### 파일정보 응답
```json
{"ownerDomainCode":"19103e6354655886cb2f46880a4ae116","ownerAuthenticationCode":"41a11f24348d2c513c5f0acac52d3531","fileName":"95b02285-a79a-4b7e-944d-c53d73b8907d.txt","fileDownloadUri":"/file-download/95b02285-a79a-4b7e-944d-c53d73b8907d.txt","fileType":"text/plain","originFileName":"hello.txt","checkSum":"65a8e27d8879283831b664bd8b7f0ad4","size":13,"filePermissions":["owner:write","owner:read"],"thumbnailImagePaths":null,"filePermissionGroups":[{"idAccessCode":"$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju"},{"idAccessCode":"f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7"},{"idAccessCode":"fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a"}]}
```
#### 파일 다운로드 요청
```
    {
       "filePhyName":"2d60c7b2-7b42-4745-9829-817287376c36.txt",
       "ownerDomainCode":"19103e6354655886cb2f46880a4ae116",
       "ownerAuthenticationCode":"41a11f24348d2c513c5f0acac52d3531",
       "fileCheckSum":"65a8e27d8879283831b664bd8b7f0ad4"
    }
```
#### 파일 다운로드 응답
``` json
{
   "ownerDomainCode":"19103e6354655886cb2f46880a4ae116",
   "ownerAuthenticationCode":"41a11f24348d2c513c5f0acac52d3531",
   "fileName":"4446de97-f770-4434-9665-25d3934c9beb.txt",
   "fileDownloadUri":"/file-download/4446de97-f770-4434-9665-25d3934c9beb.txt",
   "fileType":"text/plain",
   "originFileName":"hello.txt",
   "checkSum":"65a8e27d8879283831b664bd8b7f0ad4",
   "size":13,
   "filePermissions":[
      "owner:write",
      "owner:read",
      "group:read",
      "group:write"
   ],
   "thumbnailImagePaths":[
      
   ],
   "filePermissionGroups":[
      "$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju",
      "f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7",
      "fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a"
   ],
   "_links":{
      "query-file":{
         "href":"http://localhost:8080"
      },
      "self":[
         {
            "href":"http://localhost:8080/file-download/4446de97-f770-4434-9665-25d3934c9beb.txt"
         },
         {
            "href":"http://localhost:8080/4446de97-f770-4434-9665-25d3934c9beb.txt"
         }
      ]
   }
}
     fileDownloadUri : 파일 업로드시 서비스되고 있는 도메인에 대해 정보 노출 (대표도메인으로 셋팅 할 수 있게 함.)
```

  - 썸네일 요청
    - 파일명 : 파일의 명칭
    - 파일타입 : png, jpg 등
    - 썸네일 사이즈 : px 기준
      - 여러게 사이즈 제작 요청 가능 (최대 5개)
      - 정사각형으로 정사각형이 아닌 이미지의 경우 비율에 맞게 이미지를 자르고 리사징 처리 진행
    -  컨텐츠 사이즈 : 가로 폭 px 기준, 최대 처리 기준 초과시 예외 발생. 0일경우 원본비율로 조정, 수량을 줄경우 준데로 처리 진행
```

    {  
        "파일명" :"aaa.jpg",
        "파일타입"  : "image/png",
        "썸내일사이즈" : [200, 400, 300], 
        "컨텐츠사이즈" : [1024, 0]
        
    },
     
```

- 썸네일 응답
``` json
{
   "ownerDomainCode":"19103e6354655886cb2f46880a4ae116",
   "ownerAuthenticationCode":"41a11f24348d2c513c5f0acac52d3531",
   "fileName":"11dea77c-ce10-494e-845f-ff58793d87df.jpg",
   "fileDownloadUri":"/public-file-download/11dea77c-ce10-494e-845f-ff58793d87df.jpg",
   "fileType":"image/jpeg",
   "originFileName":"IMG_3421.jpg",
   "checkSum":"25285cef31548d48cf98a8dba896eab0",
   "size":2007265,
   "filePermissions":[
      "owner:write",
      "owner:read",
      "public:read"
   ],
   "thumbnailImagePaths":[
      "/public-file-download/11dea77c-ce10-494e-845f-ff58793d87df_200.jpg",
      "/public-file-download/11dea77c-ce10-494e-845f-ff58793d87df_400.jpg",
      "/public-file-download/11dea77c-ce10-494e-845f-ff58793d87df_500.jpg"
   ],
   "filePermissionGroups":[
      {
         "idAccessCode":"$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju"
      },
      {
         "idAccessCode":"f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7"
      },
      {
         "idAccessCode":"fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a"
      }
   ]
}
```
- 썸네일 파일 요청
``` json
   {
   "ownerDomainCode":"19103e6354655886cb2f46880a4ae116",
   "ownerAuthenticationCode":"41a11f24348d2c513c5f0acac52d3531",
   "fileName":"58262f09-8209-400d-8c67-c27d4f7c82a5_200.jpg",
   "fileDownloadUri":"/public-file-download/58262f09-8209-400d-8c67-c27d4f7c82a5_200.jpg",
   "fileType":"image/jpeg",
   "originFileName":"58262f09-8209-400d-8c67-c27d4f7c82a5.jpg",
   "checkSum":"25285cef31548d48cf98a8dba896e1212",
   "size":19000,
   "filePermissions":[
      "owner:write",
      "owner:read",
      "public:read"
   ],
   "filePermissionGroups":[
      {"idAccessCode":"$2a$10$TuKGiVuLJl3xhaVPDNj3EOcjDyKrMcFcc7m.d.PsFX7UjbTgrl1Ju"},
      {"idAccessCode":"f52fbd32b2b3b86ff88ef6c490628285f482af15ddcb29541f94bcf526a3f6c7"},
      {"idAccessCode":"fb8c2e2b85ca81eb4350199faddd983cb26af3064614e737ea9f479621cfa57a"}
   ]
}
```


### 테스트 방법
#### Junit Test Case
- FilControllerTest 실행
  - 참고 : https://theheydaze.tistory.com/218

#### web ui
- http://localhost:8080/demo-file-upload.html 호출 후 해당 필드 입력하여 전송
    - 참고 : Server 측 셋팅 추가 
```
Access-Control-Allow-Orgin : 요청을 보내는 페이지의 출처 (*, 도메인)
Access-Control-Allow-Methods : 요청을 허용하는 메소드 (Default : GET, POST, HEAD)
Access-Control-Max-Age : 클라이언트에서 pre-flight의 요청 결과를 저장할 시간 지정. 해당 시간 동안은 pre-flight를 다시 요청하지 않는다.
Access-Control-Allow-Headers : 요청을 허용하는 헤더
```
#### DB 확인
```sql
SELECT * FROM `file-apis`.file_permission_group;

SELECT * FROM `file-apis`.my_files;

SELECT * FROM `file-apis`.file_permission_group;

SELECT * FROM `file-apis`.file_shares;

SELECT * FROM `file-apis`.file_owner;
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