# class 101 homework

* required\
java 1.8

* gradle run\
```gradlew bootRun```
* build\
```gradlew build```
* jar start\
```java -jar -Dfile.encoding=UTF-8 build/libs/homework1-1.0-SNAPSHOT.jar```

* folder structure\
```
-------.circleci github연동 CI/CD tool
   |
   |---.git
   |---.github github템플릿 폴더
   |---gradle
src--main
   |  |     
   |  |----java
   |  |     |
   |  |     |---constants  메시지 등의 상수 모음 클래스
   |  |     |-data
   |  |     |   |--beans 서비스영역에서 소비되는 패키지
   |  |     |   |--entity 데이터베이스에 저장되는 테이블 구조 패키지
   |  |     |   |--repository 데이터베이스에 접근하는 인터페이스 패키지
   |  |     |---exceptions 예외처리 패키지
   |  |-----resources 환경설정 폴더
   |   
   |--test
        |----data.repository 데어터 계층수준의 단위 테스트
        |---- 서비스 단위 단위테스트 및 통합테스트
   
```     

* description\
```
상품을 orderService.main을 통해서 주문할 것인지 확인합니다.
orderProcessor에서 상품목록을 조회한 후
원하는 상품번호를 입력하면 희망상품(wishProduct)에 담습니다.
다음으로 원하는 수량을 입력하면 희망상품은 카트에 담겨집니다.

단, 희망상품에 담길때는 카트에 같은 물건이 있는지
확인하고 같은 상품이 있다면 교체하는 작업이 있습니다.
여기서 클래스 상품의 교체는 제외하게 하였습니다.

또한 주문중 종료를 위해
상품번호 입력, 수량입력시 q + enter 버튼을 누르게 되면
종료하도록 하였습니다.
```