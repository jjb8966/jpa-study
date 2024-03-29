package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);

    //@Query(name = "Member.findByUsername") -> 없어도 동작함
    List<Member> findByUsername(@Param("username") String username);

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    @Query("select m.username from Member m")
    List<String> findUsernameList();

    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") List<String> names);

    // 다양한 리턴타입을 지원
    List<Member> findListByUsername(String username);           // 컬렉션 리턴

    Member findMemberByUsername(String username);               // 단건 리턴

    Optional<Member> findOptionalByUsername(String username);   // 단건(Optional) 리턴

    /**
     * 페이징 & 정렬
     *  - 파라미터 -> Pageable, Sort
     *  - 리턴 타입 -> Page, Slice, List
     * Page -> count 쿼리 o
     * Slice -> count 쿼리 x, 다음 페이지 확인 o
     * List -> count 쿼리 x, 결과만 조회
     */
    // 페이징 + 정렬
//    Page<Member> findByAgeGreaterThanEqual(int age, Pageable pageable);
    Slice<Member> findByAgeGreaterThanEqual(int age, Pageable pageable);

    // 정렬만
    Slice<Member> findByAgeGreaterThanEqual(int age, Sort sort);
    //List<Member> findByAgeGreaterThan(int age, Sort sort);

    /**
     * 다른 테이블과 조인한 결과에서 페이징 해야할 경우
     * 조인한 결과에서 카운팅하므로 카운팅 쿼리로 인한 성능이 저하가 있을 수 있음
     * -> 카운팅 쿼리를 분리하여 해결함
     */
    @Query(value = "select m from Member m left join m.team t where m.age >= :age",
            countQuery = "select count(m) from Member m")
    Page<Member> findByAge(@Param("age") int age, Pageable pageable);
    //Slice<Member> findByAge(int age, Pageable pageable);
    //List<Member> findByAge(int age, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    // Member 조회 시 Team을 fetch join 하는 메소드들
    // 1. 직접 fetch join 쿼리 작성
    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    // 2. @EntityGraph + findAll() 메소드 오버라이딩
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    // 3. @EntityGraph + @Query
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findEntityGraph();

    // 4. @EntityGraph + 쿼리 메소드
    @EntityGraph(attributePaths = "team")
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Member findLockByUsername(String username);

    List<ProjectionUsernameOnly> findProjectionByUsername(String username);

    List<ProjectionUsernameAndAgeAndTeamName> findMoreProjectionByUsername(String username);

    <T> List<T> findClassProjectionByUsername(String username, Class<T> type);

    @Query(value = "select * from member where username = ?", nativeQuery = true)
    Member findByNativeQuery(String username);

    @Query(value = "select m.member_id as id, m.username, t.name as teamName" +
            " from member m left join team t",
            countQuery = "select count(*) from member",
            nativeQuery = true)
    Page<MemberProjection> findByNativeProjection(Pageable pageable);

}
