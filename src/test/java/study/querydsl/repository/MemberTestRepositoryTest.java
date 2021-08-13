package study.querydsl.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;
import study.querydsl.dto.querydsl.MemberSearchCondition;
import study.querydsl.dto.querydsl.MemberTeamDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberTestRepositoryTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private MemberTestRepository memberTestRepository;

	@BeforeEach
	void setup() {
		Team teamA = new Team("Team1");
		Team teamB = new Team("Team2");
		entityManager.persist(teamA);
		entityManager.persist(teamB);

		for (int i = 1; i <= 10; ++i) {
			Team selectedTeam = i % 2 != 0 ? teamA : teamB;
			entityManager.persist(new Member("member" + i, 10 + i * 5, selectedTeam));
		}
		entityManager.flush();
	}

	@Test
	@DisplayName("basicSelect")
	void basicSelect() {
		List<Member> result = memberTestRepository.basicSelect();
		assertThat(result.size()).isEqualTo(10);
	}

	@Test
	@DisplayName("basicSelectFrom")
	void basicSelectFrom() {
		List<Member> result = memberTestRepository.basicSelectFrom();
		assertThat(result.size()).isEqualTo(10);
	}

	@Test
	@DisplayName("search by paging")
	void searchByPaging() {
		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setTeamName("Team1");
		condition.setAgeGoe(20);
		condition.setAgeLoe(40);
		PageRequest pageRequest = PageRequest.of(0, 3);

		Page<Member> result = memberTestRepository.searchPageByApplyPage(condition, pageRequest);
		List<Member> members = result.toList();
		for (Member member : members) {
			System.out.println("member = " + member);
		}

		assertThat(result.toList().size()).isEqualTo(2);
		assertThat(result).extracting("name")
			.containsExactly("member3", "member5");
	}

	@Test
	@DisplayName("search by apply pagination")
	void searchByApplyPagination() {
		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setTeamName("Team1");
		condition.setAgeGoe(10);
		condition.setAgeLoe(50);
		PageRequest pageRequest = PageRequest.of(0, 3);

		Page<MemberTeamDto> result = memberTestRepository.applyPagination(condition, pageRequest);

		// total은 조회한 총 개수가 담겨있으므로 15,25,35,45 총 4개가 된다.
		assertThat(result.getTotalElements()).isEqualTo(4);

		List<MemberTeamDto> findMembers = result.toList();
		// list에는 paging된 결과가 담겨있고, 페이지 크기가 3이므로 list의 개수는 3이다.
		assertThat(findMembers.size()).isEqualTo(3);
		assertThat(findMembers).extracting("memberName")
			.containsExactly("member1", "member3", "member5");
	}

	@Test
	@DisplayName("search by apply pagination 2")
	void searchByApplyPagination2() {
		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setTeamName("Team1");
		condition.setAgeGoe(10);
		condition.setAgeLoe(100);
		PageRequest pageRequest = PageRequest.of(1, 3);

		Page<MemberTeamDto> result = memberTestRepository.applyPagination2(condition, pageRequest);
		for (MemberTeamDto memberTeamDto : result) {
			System.out.println("memberTeamDto = " + memberTeamDto);
		}
		// total은 조회한 총 개수가 담겨있으므로 15, 25, 35, 45, 55
		// total은 페이징에 영향받지 않는다
		assertThat(result.getTotalElements()).isEqualTo(5);

		List<MemberTeamDto> findMembers = result.toList();
		// list에는 paging된 결과가 담겨있고, 페이지 크기가 3이지만, 1페이지(index=0)에서 3개가 검색되었으므로 나머지 2개가 list에 담긴다
		assertThat(findMembers.size()).isEqualTo(2);
		assertThat(findMembers).extracting("memberName")
			.containsExactly("member7", "member9");

	}
}