package study.querydsl;

import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

//@Rollback(false)
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

	// QueryDSL??? JPQL??? Builder ?????? ??????
	@Test
	void startQuerydsl() {
		// 1. Q-Type Object ?????? ???????????? ??????
		// ?????? ???????????? Join??? ???????????? alias ????????? ????????? ???????????? (1)??? ????????? ?????? ????????? alias??? ?????? ???????????? ???????????? ??????.
		//QMember m = new QMember("m");

		// 2. ?????? ???????????? static instance ??????
		//QMember m = QMember.member;
		// 3. ?????? ???????????? static instance + static import ??????
		Member findMember = queryFactory
			.select(member)
			.from(member)
			.where(member.name.eq("member1"))
			.fetchOne();

		assertThat(findMember.getName()).isEqualTo("member1");
	}

	@Test
	@DisplayName("??????: and")
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
	@DisplayName("??????: simple and")
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
	@DisplayName("??????: and, in, goe, lt")
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
	@DisplayName("??????: or, between")
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
	@DisplayName("?????? ??????")
	void resultFetch() {
		List<Member> fetch = queryFactory
			.selectFrom(member)
			.fetch();
		assertThat(fetch.size()).isEqualTo(4);

		// ?????? ???????????? persistence??? ????????? querydsl??????.
		assertThatThrownBy(() -> {
			Member fetchOne = queryFactory
				.selectFrom(member)
				.fetchOne();
		}).isInstanceOf(NonUniqueResultException.class);

		Member fetchFirst = queryFactory
			.selectFrom(member)
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
	@DisplayName("??????")
	void sort() {
		entityManager.persist(new Member("member6", 90));
		entityManager.persist(new Member("member7", 90));
		entityManager.persist(new Member("member8", 90));
		entityManager.persist(new Member(null, 100));
		entityManager.persist(new Member(null, 50));

		// ?????? ??????
		// 1. ?????? ????????????
		// 2. ?????? ????????????
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
	@DisplayName("?????????")
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
		// offset = 2?????????, 0, 1?????? ???????????? ????????????.
		// ????????? member6, member5??? ???????????? member4?????? ????????????.
		// Spring-Data-Jpa??? ?????????????????? offset, size??? ????????? ??????????????? ??? size??? querydsl??? limit??? ????????? ??????????????????.
		// querydsl??? ?????? ??????????????? offset?????? ???????????? ???????????? ???????????? ???????????????
		// spring-data-jpa pagable??? size??? size * offset?????? ???????????? ????????? ???????????? ????????????.
		assertThat(fetch.get(0).getName()).isEqualTo("member4");
		assertThat(fetch.get(1).getName()).isEqualTo("member3");
	}


	@Test
	@DisplayName("?????????2")
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
		// total ????????? ?????? ??? limit, offset??? ???????????? ?????????
		//assertThat(memberQueryResults.getTotal()).isEqualTo(2);
		assertThat(fetch.size()).isEqualTo(2);
		assertThat(fetch.get(0).getName()).isEqualTo("member4");
		assertThat(fetch.get(1).getName()).isEqualTo("member3");
	}

	@Test
	@DisplayName("??????: count, sum, max, min, avg")
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
	@DisplayName("??????: groupby, having")
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
	@DisplayName("??????")
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
	@DisplayName("?????? ??????(?????? ????????? ?????? ??????????????? ??????)")
	void thetaJoin() {
		entityManager.persist(new Member("TeamA"));
		entityManager.persist(new Member("TeamB"));
		entityManager.persist(new Member("TeamC"));

		// cross join??? ????????????.
		List<Member> result = queryFactory
			.select(member)
			.from(member, team)
			.where(member.name.eq(team.name))
			.fetch();

		assertThat(result).extracting("name").containsExactly("TeamA", "TeamB");
	}

	@Test
	@DisplayName("?????? + ?????????")
	void joinOnFiltering() {
		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			// left-join????????? team.name??? TeamA??? ?????? ???????????? ?????? ????????????.
			.leftJoin(member.team, team).on(team.name.eq("TeamA"))

			// inner-join????????? team.name??? TeamA??? ?????? ????????? ???????????? ????????????.
			//.join(member.team, team).on(team.name.eq("TeamA"))
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("member_name: " + tuple);
		}
	}

	@Test
	@DisplayName("??????????????? ?????? ???????????? ?????? + on")
	void joinOnNoRelation() {
		entityManager.persist(new Member("TeamA"));
		entityManager.persist(new Member("TeamB"));
		entityManager.persist(new Member("TeamC"));

		List<Tuple> result = queryFactory
			.select(member, team)
			.from(member)
			.leftJoin(team).on(member.name.eq(team.name))
			// ??????????????? ?????? ???????????? join?????? on?????? ????????? ??? ????????? inner, outer join ?????? ????????????. (hibernate 5.1??????)
			//.join(team).on(member.name.eq(team.name))
			.fetch();

		assertThat(result.size()).isEqualTo(7);
		for (Tuple tuple : result) {
			System.out.println("tuple: " + tuple);
		}
	}

	@Test
	@DisplayName("?????? ?????? ?????????")
	void fechjoinNotUse() {
		entityManager.flush();
		entityManager.clear();

		List<Member> result = queryFactory
			.selectFrom(member)
			.join(member.team, team)
			.where(member.name.eq("member1"))
			.fetch();

		boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(result.get(0).getTeam());
		assertThat(loaded).isFalse();
	}

	@Test
	@DisplayName("?????? ?????? ??????")
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

	// ????????? ?????? ?????? ?????? ??????
	@Test
	@DisplayName("???????????? - where???")
	void subQueryWhere() {
		QMember memberSub = new QMember("memberSub");

		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.age.eq(
				select(memberSub.age.max()).from(memberSub)
			))
			.fetchOne();

		assertThat(findMember.getAge()).isEqualTo(40);
		assertThat(findMember.getName()).isEqualTo("member4");
	}

	// ?????? ?????? ????????? ????????? ??????
	@Test
	@DisplayName("???????????? - where???2")
	void subQueryWhere2() {
		QMember memberSub = new QMember("memberSub");
		List<Member> members = queryFactory
			.selectFrom(member)
			.where(member.age.in(
				JPAExpressions
					.select(memberSub.age)
					.from(memberSub)
					.where(memberSub.age.gt(20))
			))
			.fetch();

		assertThat(members.size()).isEqualTo(2);
		assertThat(members).extracting("age").containsExactly(30, 40);
	}

	// ??????????????? ????????????????????? ??????
	@Test
	@DisplayName("???????????? - select???")
	void subQuerySelect() {
		QMember memberSub = new QMember("memberSub");

		List<Tuple> result = queryFactory
			.select(member.name,
				JPAExpressions
					.select(memberSub.age.avg())
					.from(memberSub)
			)
			.from(member)
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple: " + tuple);
		}
	}

	@Test
	@DisplayName("Simple Case")
	void simpleCase() {
		List<String> result = queryFactory
			.select(member.age
				.when(10).then("10???")
				.when(20).then("20???")
				.otherwise("??? ???"))
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println(s);
		}
	}

	// ?????? ?????????????????? ?????? ?????? ???????????? ????????? DB?????? ??????????????? ???????????????????????? ?????????????????? ??????.
	// DB??? ????????? Raw Data??? ????????????, ??????/????????? App?????? ?????????!
	@Test
	@DisplayName("Complex Case")
	void complexCase() {
		List<String> result = queryFactory
			.select(new CaseBuilder()
				.when(member.age.between(10, 19)).then("10???")
				.when(member.age.between(20, 29)).then("20???")
				.otherwise("??????")
			)
			.from(member)
			.fetch();

		for (String s : result) {
			System.out.println(s);
		}
	}

	@Test
	@DisplayName("?????? ?????????")
	void addConstant() {
		List<Tuple> result = queryFactory
			.select(member.name, Expressions.constant("AAA"))
			.from(member)
			.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@Test
	@DisplayName("?????? ?????????")
	void addString() {
		// username_age ??? ????????? ????????????
		List<String> result = queryFactory
			//.select(member.name.concat("_").concat(String.valueOf(member.age)))
			.select(member.name.concat("_").concat(member.age.stringValue()))
			.from(member)
			.where(member.age.gt(20))
			.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}
}
