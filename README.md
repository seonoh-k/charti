CHARTI

- 프로젝트 기한 - 7월 3일까지
- 서비스 호스팅 - Amazon AWS EC2
- 데이터베이스 - Amazon AWS RDS (MySQL)
- 스토리지 - CloudFlare R2 (개체 스토리지)

application.properties 파일에
spring.datasource.username=user1
유저 1부터 6까지 있습니다. 2부터 6까지 각자 하나씩 정해주세요.

MVP 모듈화
1. BaseEntity
   - 대부분의 테이블에서 저장하는 상태, 생성일, 삭제일을 공통적으로 관리하기 위해 작성
   - 이 클래스를 상속 받은 엔티티 클래스에는 상태, 생성일, 삭제일을 작성하지 않아도 됨
   - 엔티티 타입.isDeleted() = 삭제된 정보라면 true, 아니라면 false
   - 엔티티 타입.markAsDeleted() = 이 메소드를 실행한 시간을 삭제일에 저장하고 활성화 상태를 true로 변경.
   - 엔티티 타입.restore() = 저장된 삭제일 데이터를 삭제하고 활성화 상태를 false로 변경. -> 정보 복구
2. BaseService
   - 서비스 클래스에서 공통적으로 사용할 CRUD를 모듈화한 추상 클래스
   - 상속 받은 모든 클래스에서 기본적인 CRUD 메소드 사용 가능
   - 다만 생성, 수정의 경우 테이블마다 다른 정보를 입력해야하기 때문에 그에따른 메소드 작성은 필수
   - 상속 예시 : public class 클래스명 extends MainService<엔티티 타입, 리포지토리명>
   - 사용 예시
   - public class ChildService extends BaseService<Child, ChildRepository> {

       // 리포지토리 생성자
       public ChildService(ChildRepository repository) { super(repository); }

       // 수정 메소드 작성 예시
       public void updateChild(Integer id, String name, String nickname) {
         // 부모 클래스의 메소드로 데이터 조회
         Child child = super.get(id);
         child.setName(name);
         child.setNickname(nickname);
         // 부모 클래스의 메소드로 데이터 수정
         super.update(child);
       }
     }
   - BaseService 때문에 리포지토리 객체 생성이 안되던 문제 해결. @RequiredArgsConstructor 어노테이션은 적용이 안됨.
3. APIResponse
   - 거의 모든 컨트롤러에서 사용할 수 있는 API 응답 모듈
   - RestController에 작성할 API의 응답을 일관된 방식으로 작성하기 위해 모듈 작성
   - RestController에 메소드 작성시 다음과 같이 사용
   - public ResponseEntity<APIResponse<Child>> 메소드명()
   - 메소드 동작 성공 시 - return ResponseEntity<APIResponse.ok(엔티티 객체 등 반환할 데이터)>
   - 메소드 동작 실패 시 - return ResponseEntity<APIResponse.error("에러 메세지 입력")>
4. GlobalExceptionHandler
   - 예외처리 모듈화
   - 컨트롤러에서 발생한 예외를 가로채 처리해주는 클래스
   - @RestControllerAdvice 어노테이션 사용으로 모든 클래스에서 발생하는 예외처리가 가능
   - 이 클래스로 인해 기능 구현 시 try-catch로 감쌀 필요가 없음
   - 현재는 일반적으로 자주 발생하는 예외만 처리하고 있기 때문에, 이 클래스에서 처리하지 못하는 예외 발생 시 알려주기 바람