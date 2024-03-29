package study.querydsl.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void 순수_JPA() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2)
                .extracting("username")
                .containsExactly("member1");
    }

    @Test
    void querydsl() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberJpaRepository.findAll_querydsl();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByUsername_querydsl("member1");
        assertThat(result2)
                .extracting("username")
                .containsExactly("member1");
    }

    @Test
    void 동적쿼리_BooleanBuilder() {
        init();

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setUsername("member3");
        condition.setAgeGoe(25);
        condition.setAgeLoe(35);

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(condition);

        assertThat(result)
                .extracting("username")
                .containsExactly("member3");
    }

    @Test
    void 동적쿼리_where() {
        init();

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setUsername("member3");
        condition.setAgeGoe(25);
        condition.setAgeLoe(35);

        List<MemberTeamDto> result = memberJpaRepository.search(condition);

        assertThat(result)
                .extracting("username")
                .containsExactly("member3");
    }

    private void init() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }
}