package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static study.querydsl.domain.QMember.member;

@Transactional
@SpringBootTest
class QuerydslMiddleTest {

	@PersistenceContext
	private EntityManager entityManager;

	private JPAQueryFactory queryFactory;

	@BeforeEach
	void setup() {
		queryFactory = new JPAQueryFactory(entityManager);

		Team teamA = new Team("Team1");
		Team teamB = new Team("Team2");
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
	@DisplayName("간단한 tuple 프로젝션 조회")
	void simpleTupleProjection() {
		List<Tuple> result = queryFactory
			.select(member.name, member.age)
			.from(member)
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("result: " + tuple.get(member.name) + "," + tuple.get(member.age));
			// id와 같이 프로젝션에 포함되지 않은 컬럼은 튜플에서 조회할 수 없다. (null)
			//System.out.println("result: " + tuple.get(member.name) + "," + tuple.get(member.id));
		}
	}
}
