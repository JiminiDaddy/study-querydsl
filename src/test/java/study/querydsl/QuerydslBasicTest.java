package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {
	@PersistenceContext
	private EntityManager entityManager;

	private JPAQueryFactory queryFactory;

	@BeforeEach
	void setup() {
		queryFactory = new JPAQueryFactory(entityManager);

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
	}

	@Test
	void startJPQL() {
		// find member1
		Member findMember = entityManager.createQuery("select m from Member m where m.name = :name", Member.class)
			.setParameter("name", "member1")
			.getSingleResult();

		assertThat(findMember.getName()).isEqualTo("member1");
	}

	@Test
	void startQuerydsl() {
		QMember m = new QMember("m");

		Member findMember = queryFactory
			.select(m)
			.from(m)
			.where(m.name.eq("member1"))
			.fetchOne();

		assertThat(findMember.getName()).isEqualTo("member1");
	}
}
