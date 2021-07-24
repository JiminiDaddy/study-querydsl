package study.querydsl.domain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


//@Rollback(false)
@Transactional
@SpringBootTest
class MemberTest {
	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void testEntity() {
		Team teamA = new Team("TeamA");
		Team teamB = new Team("TeamB");
		entityManager.persist(teamA);
		entityManager.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		entityManager.persist(member1);
		entityManager.persist(member2);
		entityManager.persist(member3);
		entityManager.persist(member4);
		entityManager.flush();
		entityManager.clear();

		List<Member> findMembers = entityManager.createQuery("select m from Member m", Member.class).getResultList();
		for (Member findMember : findMembers) {
			System.out.println("findMember = " + findMember);
			System.out.println("findMember.team = " + findMember.getTeam());
		}
	}
}