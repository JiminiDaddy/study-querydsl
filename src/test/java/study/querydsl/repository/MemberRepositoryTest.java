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
import study.querydsl.repository.query.MemberQueryRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberRepositoryTest {
	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private MemberRepository memberRepository;

	// 특정 조회용 API를 구현할 땐 별도의 Query용 Repository를 구현하여 사용할 수 있다.
	@Autowired
	private MemberQueryRepository memberQueryRepository;

	private List<Long> memberIds = new ArrayList<>();

	@BeforeEach
	void setup() {
		Team teamA = new Team("Team1");
		Team teamB = new Team("Team2");
		entityManager.persist(teamA);
		entityManager.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		Member member5 = new Member("member5", 25, teamA);
		Member member6 = new Member("member6", 35, teamB);

		entityManager.persist(member1);
		entityManager.persist(member2);
		entityManager.persist(member3);
		entityManager.persist(member4);
		entityManager.persist(member5);
		entityManager.persist(member6);
		entityManager.flush();

		memberIds.add(member1.getId());
		memberIds.add(member2.getId());
		memberIds.add(member3.getId());
		memberIds.add(member4.getId());
		memberIds.add(member5.getId());
		memberIds.add(member6.getId());

	}

	@Test
	@DisplayName("JPA Basic TEST")
	void jpaBasicTest() {
		Member findMember = memberRepository.findById(memberIds.get(0)).orElseThrow(
			() -> new IllegalArgumentException("WrongId: " + memberIds.get(0)));
		assertThat(findMember.getName()).isEqualTo("member1");

		List<Member> findAll = memberRepository.findAll();
		assertThat(findAll.size()).isEqualTo(6);

		List<Member> findMember2 = memberRepository.findByName("member2");
		assertThat(findMember2.size()).isEqualTo(1);
		assertThat(findMember2.get(0).getAge()).isEqualTo(20);
	}

	@Test
	@DisplayName("search")
	void searchTest() {
		MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
		//memberSearchCondition.setMemberName("member3");
		memberSearchCondition.setTeamName("Team2");
		memberSearchCondition.setAgeGoe(30);
		memberSearchCondition.setAgeLoe(35);
		List<MemberTeamDto> result = memberRepository.search(memberSearchCondition);
		//List<MemberTeamDto> result = memberQueryRepository.search(memberSearchCondition);
		for (MemberTeamDto memberTeamDto : result) {
			System.out.println("memberTeamDto = " + memberTeamDto);
		}

		assertThat(result).extracting("memberName").containsExactly("member3", "member6");
	}

	@Test
	@DisplayName("search with page")
	void searchWithPage() {
		MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
		memberSearchCondition.setTeamName("Team2");

		PageRequest pageRequest = PageRequest.of(0, 3);

		Page<MemberTeamDto> result = memberRepository.searchPagingSimple(memberSearchCondition, pageRequest);
		assertThat(result.getSize()).isEqualTo(3);
		assertThat(result).extracting("memberName")
			.containsExactly("member3", "member4", "member6");
	}

	@Test
	@DisplayName("search with page complex")
	void searchWithPageComplex() {
		MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
		memberSearchCondition.setTeamName("Team2");

		PageRequest pageRequest = PageRequest.of(0, 3);

		Page<MemberTeamDto> result = memberRepository.searchPagingComplex(memberSearchCondition, pageRequest);
		assertThat(result.getSize()).isEqualTo(3);
		assertThat(result).extracting("memberName")
			.containsExactly("member3", "member4", "member6");
	}

	@Test
	@DisplayName("search with page count optimizing")
	void searchWithPageComplexAndCountOptimizing() {
		// Spring-Data의 지원으로, total count를 위한 Query가 필요없는경우는 count query가 DB로 전송되지 않아야 한다
		MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
		memberSearchCondition.setTeamName("Team2");

		PageRequest pageRequest = PageRequest.of(0, 100);
		Page<MemberTeamDto> result = memberRepository.searchPagingComplexBySpringData(memberSearchCondition, pageRequest);
		assertThat(result.toList().size()).isEqualTo(3);
		assertThat(result).extracting("memberName")
			.containsExactly("member3", "member4", "member6");

		pageRequest = PageRequest.of(1, 2);
		result = memberRepository.searchPagingComplexBySpringData(memberSearchCondition, pageRequest);
		assertThat(result.toList().size()).isEqualTo(1);
		assertThat(result).extracting("memberName")
			.containsExactly("member6");

	}
}