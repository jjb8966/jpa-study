package study.datajpa.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;

    @Test
    public void testMember() {
        // 프록시 객체
        System.out.println("memberRepository = " + memberRepository.getClass());

        Member member = new Member("memberA");

        Member savedMember = memberRepository.save(member);
        em.flush();

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember).isEqualTo(savedMember);
    }

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deleteCount = memberRepository.count();
        assertThat(deleteCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("bbb", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("bbb");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    // 거의 안씀
    public void testNamedQuery() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("aaa");
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void testQuery() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("aaa", 10);
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> result = memberRepository.findUsernameList();

        for (String name : result) {
            System.out.println("name = " + name);
        }
    }

    @Test
    public void findMemberDto() {
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member member = new Member("aaa", 10);
        member.setTeam(team);
        memberRepository.save(member);

        List<MemberDto> result = memberRepository.findMemberDto();

        for (MemberDto memberDto : result) {
            System.out.println("dto = " + memberDto);
        }
    }

    @Test
    public void findByNames() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("aaa", "bbb"));

        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType() {
        Member m1 = new Member("aaa", 10);
        Member m2 = new Member("bbb", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // 정상 조회
        List<Member> result1 = memberRepository.findListByUsername("aaa");
        Member result2 = memberRepository.findMemberByUsername("aaa");
        Optional<Member> result3 = memberRepository.findOptionalByUsername("aaa");
        System.out.println("result1 = " + result1); // result1 = [Member(id=1, username=aaa, age=10)]
        System.out.println("result2 = " + result2); // result2 = Member(id=1, username=aaa, age=10)
        System.out.println("result3 = " + result3); // result3 = Optional[Member(id=1, username=aaa, age=10)]

        // 없는 데이터 조회
        // 컬렉션은 절대 null을 반환하지 않음!!
        List<Member> result4 = memberRepository.findListByUsername("xxx");
        /*
        안좋은 코드
        if (result4 != null) {
            // 로직
        }
        */

        /**
         * 단건 조회 시 null을 리턴함
         * -> 순수 JPA는 getSingleResult() 사용 시 조회 결과가 없으면 NoResultException 예외를 던짐
         * 예외 vs null 뭐가 더 좋은가??
         * -> 큰 의미 없음. Optional을 쓰는게 가장 좋음
         */
        Member result5 = memberRepository.findMemberByUsername("xxx");
        Optional<Member> result6 = memberRepository.findOptionalByUsername("xxx");
        System.out.println("result4 = " + result4); // result4 = []
        System.out.println("result5 = " + result5); // result5 = null
        System.out.println("result6 = " + result6); // result6 = Optional.empty

        /**
         * 만약 단건 조회 시 결과가 2개 이상이라면?
         * -> NonUniqueResultException 예외 발생
         * --> 스프링 데이 JPA가 스프링 예외로 변환해서 던짐 (IncorrectResultSizeDataAccessException)
         * DB가 변경되어도 클라이언트가 스프링 예외를 처리하도록 되어있다면 코드를 변경할 필요가 없음
         */
//        Member m3 = new Member("aaa", 30);
//        memberRepository.save(m3);
//        Optional<Member> result7 = memberRepository.findOptionalByUsername("aaa");
    }

    @Test
    @DisplayName("리턴 타입은 Page이고 파라미터로 Pageable을 받는 메소드 사용")
    public void page_pageable() {
        memberRepository.save(new Member("member1", 5));
        memberRepository.save(new Member("member2", 8));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 12));
        memberRepository.save(new Member("member5", 14));
        memberRepository.save(new Member("member6", 16));
        memberRepository.save(new Member("member7", 18));

        int age = 10;

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // 리턴타입이 Slice인 경우 totalCount 구하지 않음
        Slice<Member> slice = memberRepository.findByAgeGreaterThanEqual(age, pageRequest);

        // 엔티티 -> DTO
        Slice<MemberDto> map = slice.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        List<Member> members = slice.getContent();
//        long totalCount = slice.getTotalElements();

        for (Member member : members) {
            System.out.println("member = " + member);
        }

        assertThat(members.size()).isEqualTo(3);

        assertThat(slice.getNumber()).isEqualTo(0);      // 페이지 숫자
        assertThat(slice.isFirst()).isTrue();                    // 첫번째 페이지인지
        assertThat(slice.hasNext()).isTrue();                    // 다음 페이지가 있는지
    }

    @Test
    @DisplayName("리턴 타입은 Slice이고 Sort를 받는 메소드 사용")
    public void paging() {
        memberRepository.save(new Member("member1", 5));
        memberRepository.save(new Member("member2", 8));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 12));
        memberRepository.save(new Member("member5", 14));
        memberRepository.save(new Member("member6", 16));
        memberRepository.save(new Member("member7", 18));

        int age = 10;

        Sort sort = Sort.by(Sort.Direction.DESC, "username");

        // 리턴타입이 Slice인 경우 totoalCount는 조회하지 않음
        Slice<Member> slice = memberRepository.findByAgeGreaterThanEqual(age, sort);

        // 엔티티 -> DTO
        Slice<MemberDto> map = slice.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

        List<Member> members = slice.getContent();
//        long totalCount = slice.getTotalElements();

        for (Member member : members) {
            System.out.println("member = " + member);
        }

        // 페이징 x, 정렬만
        assertThat(members.size()).isEqualTo(5);

        assertThat(slice.getNumber()).isEqualTo(0);      // 페이지 숫자
        assertThat(slice.isFirst()).isTrue();                    // 첫번째 페이지인지
    }

    @Test
    @DisplayName("여러 엔티티를 조인하여 조회하는 경우")
    @Commit
    public void count() {
        memberRepository.save(new Member("member1", 5));
        memberRepository.save(new Member("member2", 8));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 12));
        memberRepository.save(new Member("member5", 14));
        memberRepository.save(new Member("member6", 16));
        memberRepository.save(new Member("member7", 18));

        int age = 10;

        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        Page<Member> result = memberRepository.findByAge(age, pageRequest);

        List<Member> content = result.getContent();
        long totalCount = result.getTotalElements();

        for (Member member : content) {
            System.out.println("member = " + member);
        }

        System.out.println("totalCount = " + totalCount);

        assertThat(content.size()).isEqualTo(3);
    }

    @Test
    public void bulkUpdate() {
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 15));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 25));
        memberRepository.save(new Member("member5", 30));

        int resultCount = memberRepository.bulkAgePlus(20);

        assertThat(resultCount).isEqualTo(3);

        //em.clear(); -> @Modifying(clearAutomatically = true)

        List<Member> result = memberRepository.findByUsername("member3");
        Member findMember = result.get(0);

        assertThat(findMember.getAge()).isEqualTo(21);
    }

    @Test
    public void findMemberLazy() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        List<Member> members = memberRepository.findAll();

        // fetch join으로 N+1 문제 해결
        //List<Member> members = memberRepository.findMemberFetchJoin();
        //List<Member> members = memberRepository.findAll(); -> 오버라이딩
        //List<Member> members = memberRepository.findEntityGraph();
        //List<Member> members = memberRepository.findEntityGraphByUsername("member1");

        // N + 1 문제 발생
        // 1 -> select member 1번
        // N -> select team x N번
        for (Member member : members) {
            System.out.println("member.getUsername() = " + member.getUsername());
//            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam() = " + member.getTeam().getName());
        }
    }

    @Test
    public void queryHint() {
        Member member = new Member("member", 10);
        memberRepository.save(member);

        em.flush();
        em.clear();

//        Member findMember = memberRepository.findMemberByUsername("member");

        // 복사본(스냅샷)이 없기 때문에 변경 감지를 하지 않고 update 쿼리를 날리지 않음!!
        Member findMember = memberRepository.findReadOnlyByUsername("member");
        findMember.setUsername("newMember");

        em.flush();
    }

    @Test
    public void lock() {
        Member member = new Member("member", 10);
        memberRepository.save(member);

        em.flush();
        em.clear();

        // select for update
        Member findMember = memberRepository.findLockByUsername("member");
    }

    @Test
    public void callCustom() {
        List<Member> result = memberRepository.findMemberCustom();
    }

    @Test
    public void jpaBaseEntity() throws InterruptedException {
        Member member = new Member("member1");
        memberRepository.save(member);  // @PrePersist

        Thread.sleep(1000);
        member.setUsername("member2");

        em.flush(); // @PreUpdate
        em.clear();

        Member findMember = memberRepository.findById(member.getId()).get();

        // BaseTimeEntity만 상속했을 때
        System.out.println("findMember.getCreatedTime() = " + findMember.getCreatedDate());
        System.out.println("findMember.getUpdatedTime() = " + findMember.getLastModifiedDate());

        // BaseEntity 상속했을 때 추가되는 정보
        System.out.println("findMember.getCreatedBy() = " + findMember.getCreatedBy());
        System.out.println("findMember.getLastModifiedBy() = " + findMember.getLastModifiedBy());
    }

    @Test
    public void projection_test() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member memberA = new Member("m1", 0, teamA);
        Member memberB = new Member("m2", 0, teamA);
        em.persist(memberA);
        em.persist(memberB);

        em.flush();
        em.clear();

        List<ProjectionUsernameOnly> result1 = memberRepository.findProjectionByUsername("m1");
        List<ProjectionUsernameAndAgeAndTeamName> result2 = memberRepository.findMoreProjectionByUsername("m1");
        List<ProjectionUsernameOnlyDto> result3 = memberRepository.findClassProjectionByUsername("m1", ProjectionUsernameOnlyDto.class);

        for (ProjectionUsernameOnly username : result1) {
            System.out.println("username = " + username.getUsername());
        }

        for (ProjectionUsernameAndAgeAndTeamName data : result2) {
            System.out.println("username, age, teamName = " + data.getUsernameAndAgeAndTeamName());
        }

        for (ProjectionUsernameOnlyDto username : result3) {
            System.out.println("username = " + username.getUsername());
        }
    }

    @Test
    public void native_query_test() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member memberA = new Member("m1", 0, teamA);
        Member memberB = new Member("m2", 0, teamA);
        em.persist(memberA);
        em.persist(memberB);

        em.flush();
        em.clear();

        Member findMember = memberRepository.findByNativeQuery("m1");

        assertThat(findMember.getUsername()).isEqualTo("m1");
    }

    @Test
    public void native_projection_test() {
        Team teamA = new Team("teamA");
        em.persist(teamA);

        Member memberA = new Member("m1", 0, teamA);
        Member memberB = new Member("m2", 0, teamA);
        em.persist(memberA);
        em.persist(memberB);

        em.flush();
        em.clear();

        Page<MemberProjection> result = memberRepository.findByNativeProjection(PageRequest.of(0, 5));

        for (MemberProjection memberProjection : result) {
            System.out.println("memberProjection.getUsername() = " + memberProjection.getUsername());
            System.out.println("memberProjection.getId() = " + memberProjection.getId());
            System.out.println("memberProjection.getTeamName() = " + memberProjection.getTeamName());
        }
    }
}