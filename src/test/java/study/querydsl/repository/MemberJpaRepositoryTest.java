package study.querydsl.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;
import study.querydsl.dto.querydsl.MemberSearchCondition;
import study.querydsl.dto.querydsl.MemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {
	@Autowired
	private MemberJpaRepository memberJpaRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	@DisplayName("JPA Basic TEST")
	void jpaBasicTest() {
		Member member = new Member("member1");
		Member member2 = new Member("member2");

		memberJpaRepository.save(member);
		memberJpaRepository.save(member2);

		Member findMember = memberJpaRepository.findById(member.getId()).orElseThrow(() -> new IllegalArgumentException("WrongId: " + member.getId()));
		assertThat(findMember).isSameAs(member);


		List<Member> findAll = memberJpaRepository.findAll();
		assertThat(findAll.size()).isEqualTo(2);

		List<Member> findMember2 = memberJpaRepository.findByName("member2");
		assertThat(findMember2.size()).isEqualTo(1);
		assertThat(findMember2.get(0)).isSameAs(member2);
	}

	@Test
	@DisplayName("Querydsl Basic TEST")
	void querydslBasicTest() {
		Member member = new Member("member1");
		Member member2 = new Member("member2");

		memberJpaRepository.save(member);
		memberJpaRepository.save(member2);

		Member findMember = memberJpaRepository.findByIdWithQuerydsl(member.getId()).orElseThrow(() -> new IllegalArgumentException("WrongId: " + member.getId()));
		assertThat(findMember).isSameAs(member);

		List<Member> findAll = memberJpaRepository.findAllWithQuerydsl();
		assertThat(findAll.size()).isEqualTo(2);

		List<Member> findMember2 = memberJpaRepository.findByNameWithQuerydsl("member2");
		assertThat(findMember2.size()).isEqualTo(1);
		assertThat(findMember2.get(0)).isSameAs(member2);
	}

	@Test
	@DisplayName("???????????? by Builder")
	void dynamicQueryByBuilder() {
		Team teamA = new Team("Team1");
		Team teamB = new Team("Team2");
		entityManager.persist(teamA);
		entityManager.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		Member member5 = new Member("member5", 25, teamA);
		Member member6 = new Member("member6", 35, teamA);
		entityManager.persist(member1);
		entityManager.persist(member2);
		entityManager.persist(member3);
		entityManager.persist(member4);
		entityManager.persist(member5);
		entityManager.persist(member6);
		entityManager.flush();

		MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
		//memberSearchCondition.setMemberName("member2");
		memberSearchCondition.setTeamName("Team2");
		memberSearchCondition.setAgeGoe(35);
		memberSearchCondition.setAgeLoe(40);
		List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(memberSearchCondition);
		for (MemberTeamDto memberTeamDto : result) {
			System.out.println("memberTeamDto : <" + memberTeamDto + ">");
		}
		assertThat(result).extracting("memberName").containsExactly("member4");
	}

	@Test
	@DisplayName("???????????? by Where??? Parameters")
	void dynamicQueryByWhereMultiParameters() {
		Team teamA = new Team("Team1");
		Team teamB = new Team("Team2");
		entityManager.persist(teamA);
		entityManager.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		Member member5 = new Member("member5", 25, teamA);
		Member member6 = new Member("member6", 35, teamA);
		entityManager.persist(member1);
		entityManager.persist(member2);
		entityManager.persist(member3);
		entityManager.persist(member4);
		entityManager.persist(member5);
		entityManager.persist(member6);
		entityManager.flush();

		MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
		memberSearchCondition.setTeamName("Team2");
		memberSearchCondition.setAgeGoe(35);
		memberSearchCondition.setAgeLoe(40);

		List<MemberTeamDto> result = memberJpaRepository.search(memberSearchCondition);
		for (MemberTeamDto memberTeamDto : result) {
			System.out.println("memberTeamDto = " + memberTeamDto);
		}
		assertThat(result).extracting("memberName").containsExactly("member4");
	}


}