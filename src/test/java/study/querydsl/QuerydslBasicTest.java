package study.querydsl;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

@Rollback(false)
@Transactional
@SpringBootTest
public class QuerydslBasicTest {
	@PersistenceContext
	private EntityManager entityManager;

	@PersistenceUnit
	private EntityManagerFactory entityManagerFactory;

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

	// QueryDSL은 JPQL의 Builder 역할 수행
	@Test
	void startQuerydsl() {
		// 1. Q-Type Object 직접 생성하여 사용
		// 같은 테이블을 Join할 경우에는 alias 이름이 같으면 안되므로 (1)번 방식과 같이 엔티티 alias를 직접 명시하여 사용해야 한다.
		//QMember m = new QMember("m");

		// 2. 미리 만들어진 static instance 사용
		//QMember m = QMember.member;
		// 3. 미리 만들어진 static instance + static import 사용
		Member findMember = queryFactory
			.select(member)
			.from(member)
			.where(member.name.eq("member1"))
			.fetchOne();

		assertThat(findMember.getName()).isEqualTo("member1");
	}

	@Test
	@DisplayName("검색: and")
	void search() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.name.eq("member1")
				.and(member.age.eq(10)))
			.fetchOne();

		Assertions.assertThat(findMember.getName()).isEqualTo("member1");
		Assertions.assertThat(findMember.getAge()).isEqualTo(10);
	}

	@Test
	@DisplayName("검색: simple and")
	void search2() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
				member.name.eq("member1"),
				member.age.eq(10))
			.fetchOne();

		Assertions.assertThat(findMember.getName()).isEqualTo("member1");
		Assertions.assertThat(findMember.getAge()).isEqualTo(10);
	}

	@Test
	@DisplayName("검색: and, in, goe, lt")
	void search3() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
				member.name.in("member1", "member2"),
				member.age.goe(15))
			.fetchOne();

		Assertions.assertThat(findMember.getName()).isEqualTo("member2");
		Assertions.assertThat(findMember.getAge()).isEqualTo(20);

		List<Member> findMembers = queryFactory
			.selectFrom(member)
			.where(
				member.name.in("member1", "member2", "member3"),
				member.age.lt(25))
			.fetch();

		assertThat(findMembers.size()).isEqualTo(2);
		for (Member findMember1 : findMembers) {
			System.out.println("findMember1.getName() = " + findMember1.getName() + ", " + findMember1.getAge());
		}
	}

	@Test
	@DisplayName("검색: or, between")
	void search4() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
				member.name.eq("member1").or(member.name.eq("member2")).and(member.age.eq(20))
			)
			.fetchOne();

		Assertions.assertThat(findMember.getName()).isEqualTo("member2");
		Assertions.assertThat(findMember.getAge()).isEqualTo(20);

		List<Member> findMembers = queryFactory
			.selectFrom(member)
			.where(
				member.name.eq("member2").and(member.age.eq(20))
				.or(member.name.eq("member3"))
				.or(member.age.between(30, 40))
			)
			.fetch();

		assertThat(findMembers.size()).isEqualTo(3);
		for (Member findMember1 : findMembers) {
			System.out.println("findMember1.getName() + \",\" + findMember1.getAge() = " + findMember1.getName() + "," + findMember1.getAge());
		}
	}

	@Test
	@DisplayName("결과 조회")
	void resultFetch() {
		List<Member> fetch = queryFactory
			.selectFrom(member)
			.fetch();
		assertThat(fetch.size()).isEqualTo(4);

		// 예외 패키지가 persistence가 아니라 querydsl이다.
		assertThatThrownBy(() -> {
			Member fetchOne = queryFactory
				.selectFrom(QMember.member)
				.fetchOne();
		}).isInstanceOf(NonUniqueResultException.class);

		Member fetchFirst = queryFactory
			.selectFrom(QMember.member)
			.fetchFirst();
		assertThat(fetchFirst.getName()).isEqualTo("member1");

		QueryResults<Member> fetchResults = queryFactory
			.selectFrom(member)
			.fetchResults();

		assertThat(fetchResults.getResults().size()).isEqualTo(4);
		assertThat(fetchResults.getTotal()).isEqualTo(4);

		long fetchCount = queryFactory
			.selectFrom(team)
			.fetchCount();
		assertThat(fetchCount).isEqualTo(2);
	}

	@Test
	@DisplayName("정렬")
	void sort() {
		entityManager.persist(new Member("member6", 90));
		entityManager.persist(new Member("member7", 90));
		entityManager.persist(new Member("member8", 90));
		entityManager.persist(new Member(null, 100));
		entityManager.persist(new Member(null, 50));

		// 정렬 기준
		// 1. 나이 내림차순
		// 2. 이름 오름차순
		// 3. nulls last
		List<Member> members = queryFactory
			.selectFrom(member)
			.where(member.age.goe(50))
			.orderBy(member.age.desc(), member.name.asc().nullsLast())
			.fetch();

		assertThat(members.size()).isEqualTo(5);
		assertThat(members.get(0).getName()).isNull();
		assertThat(members.get(4).getName()).isNull();
		assertThat(members.get(1).getName()).isEqualTo("member6");
		assertThat(members.get(2).getName()).isEqualTo("member7");
		assertThat(members.get(3).getName()).isEqualTo("member8");
	}

	@Test
	@DisplayName("페이징")
	void paging() {
		entityManager.persist(new Member("member5", 50));
		entityManager.persist(new Member("member6", 60));

		List<Member> fetch = queryFactory
			.selectFrom(member)
			.orderBy(member.age.desc())
			.offset(2)
			.limit(2)
			.fetch();

		assertThat(fetch.size()).isEqualTo(2);
		// offset = 2이므로, 0, 1번째 데이터는 제외된다.
		// 따라서 member6, member5는 제외되고 member4부터 조회된다.
		// Spring-Data-Jpa의 페이징에서는 offset, size가 인자로 넘어가는데 이 size와 querydsl의 limit이 다름에 유의해야한다.
		// querydsl은 전체 데이터에서 offset만큼 데이터가 제외되고 나머지가 조회되지만
		// spring-data-jpa pagable의 size는 size * offset만큼 제외되고 나머지 데이터가 조회된다.
		assertThat(fetch.get(0).getName()).isEqualTo("member4");
		assertThat(fetch.get(1).getName()).isEqualTo("member3");
	}


	@Test
	@DisplayName("페이징2")
	void paging2() {
		entityManager.persist(new Member("member5", 50));
		entityManager.persist(new Member("member6", 60));

		QueryResults<Member> memberQueryResults = queryFactory
			.selectFrom(member)
			.orderBy(member.age.desc())
			.offset(2)
			.limit(2)
			.fetchResults();
		List<Member> fetch = memberQueryResults.getResults();
		// total 쿼리를 구할 때 limit, offset는 적용되지 않는다
		//assertThat(memberQueryResults.getTotal()).isEqualTo(2);
		assertThat(fetch.size()).isEqualTo(2);
		assertThat(fetch.get(0).getName()).isEqualTo("member4");
		assertThat(fetch.get(1).getName()).isEqualTo("member3");
	}

	@Test
	@DisplayName("집합: count, sum, max, min, avg")
	void aggregation() {
		Tuple result = queryFactory
			.select(
				member.count(),
				member.age.max(),
				member.age.min(),
				member.age.sum(),
				member.age.avg()
			)
			.from(member)
			.fetchOne();

		assertThat(result.get(member.count())).isEqualTo(4);
		assertThat(result.get(member.age.max())).isEqualTo(40);
		assertThat(result.get(member.age.min())).isEqualTo(10);
		assertThat(result.get(member.age.sum())).isEqualTo(100);
		assertThat(result.get(member.age.avg())).isEqualTo(25);
	}

	@Test
	@DisplayName("집합: groupby, having")
	void groupbyAndHaving() {
		List<Tuple> result = queryFactory
			.select(team.name, member.age.avg())
			.from(member)
			.join(member.team, team)
			.groupBy(team.name)
			.fetch();

		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertThat(teamA.get(team.name)).isEqualTo("TeamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);
		assertThat(teamB.get(team.name)).isEqualTo("TeamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
	}

	@Test
	@DisplayName("조인")
	void join() {
		List<Member> teamA = queryFactory
			.selectFrom(member)
			.join(member.team, team)
			.where(team.name.eq("TeamA"))
			.fetch();

		assertThat(teamA.size()).isEqualTo(2);
		assertThat(teamA)
			.extracting("name")
			.containsExactly("member1", "member2");
	}

	@Test
	@DisplayName("세타 조인(연관 관계가 없는 엔티티끼리 조인)")
	void thetaJoin() {
		entityManager.persist(new Member("TeamA"));
		entityManager.persist(new Member("TeamB"));
		entityManager.persist(new Member("TeamC"));

		// cross join이 발생한다.
		List<Member> result= queryFactory
			.select(member)
			.from(member, team)
			.where(member.name.eq(team.name))
			.fetch();

		assertThat(result).extracting("name").containsExactly("TeamA", "TeamB");
	}

	@Test
	@DisplayName("조인 + 필터링")
	void joinOnFiltering() {
		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			// left-join의경우 team.name이 TeamA가 아닌 멤버들도 함깨 조회된다.
			.leftJoin(member.team, team).on(team.name.eq("TeamA"))

			// inner-join의경우 team.name이 TeamA가 아닌 멤버는 검색에서 제외된다.
			//.join(member.team, team).on(team.name.eq("TeamA"))
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("member_name: " + tuple);
		}
	}

	@Test
	@DisplayName("연관관계가 없는 엔티티의 조인 + on")
	void joinOnNoRelation() {
		entityManager.persist(new Member("TeamA"));
		entityManager.persist(new Member("TeamB"));
		entityManager.persist(new Member("TeamC"));

		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			.leftJoin(team).on(member.name.eq(team.name))
			// 연관관계가 없는 엔티티의 join에서 on절을 사용할 수 있으며 inner, outer join 모두 가능하다. (hibernate 5.1이상)
			//.join(team).on(member.name.eq(team.name))
			.fetch();

		assertThat(result.size()).isEqualTo(7);
		for (Tuple tuple : result) {
			System.out.println("tuple: " + tuple);
		}
	}

	@Test
	@DisplayName("페치 조인 미사용")
	void fechjoinNotUse() {
		entityManager.flush();
		entityManager.clear();

		List<Member> result= queryFactory
			.selectFrom(member)
			.join(member.team, team)
			.where(member.name.eq("member1"))
			.fetch();

		boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(result.get(0).getTeam());
		assertThat(loaded).isFalse();
	}

	@Test
	@DisplayName("페치 조인 사용")
	void fetcjoinUse() {
		entityManager.flush();
		entityManager.clear();

		Member findMember = queryFactory
			.selectFrom(member)
			.join(member.team, team).fetchJoin()
			.where(member.name.eq("member1"))
			.fetchOne();

		 boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		 assertThat(loaded).isTrue();
	}
}
