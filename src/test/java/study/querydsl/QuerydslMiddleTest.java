package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

	@Test
	@DisplayName("JPQL를 사용해서 Dto 조회")
	void findDtoByJPQL() {
		// JPQL을 사용하면 생성자 방식만 지원된다.
		List<MemberDto> result = entityManager.createQuery(
			"select new study.querydsl.dto.MemberDto(m.name, m.age) from Member m", MemberDto.class)
			.getResultList();

		for (MemberDto member1 : result) {
			System.out.println("member1 = " + member1);
		}
	}

	@Test
	@DisplayName("Querydsl을 사용하고 Setter 방식의 Dto 조회")
	void findDtoByQuerydslSetter() {
		// Setter, 생성자, 필드 방식 지원

		List<MemberDto> result = queryFactory
			.select(Projections.bean(MemberDto.class, member.name, member.age))
			.from(member)
			.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@Test
	@DisplayName("Querydlsl을 사용하고 필드 방식의 Dto 조회")
	void findDtoByQuerydslField() {
		List<MemberDto> result = queryFactory
			.select(Projections.fields(MemberDto.class, member.name, member.age))
			.from(member)
			.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@Test
	@DisplayName("Querydsl을 사용하고 생성자 방식의 Dto 조회")
	void findDtoByQuerydslConstructor() {
		List<MemberDto> result = queryFactory
			.select(Projections.constructor(MemberDto.class, member.name, member.age))
			.from(member)
			.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@Test
	@DisplayName("UserDto 조회")
	void findUserDto() {
		List<UserDto> result = queryFactory
			//.select(Projections.fields(UserDto.class, member.name, member.age))
			// 엔티티 필드명과 Dto 필드명이 다른 경우 as를 통해 매핑시켜준다.
			.select(Projections.fields(UserDto.class, member.name.as("username"), member.age))
			.from(member)
			.fetch();

		for (UserDto userDto : result) {
			System.out.println("o = " + userDto);
		}
	}

	@Test
	@DisplayName("UserDto 조회2")
	void findUserDto2() {
		QMember memberSub = new QMember("memberSub");
		List<UserDto> result = queryFactory
			.select(Projections.fields(UserDto.class, 
				member.name.as("username"), 
				//member.age,
				Expressions.as(JPAExpressions
					.select(memberSub.age.max())
					.from(memberSub), "age")
			))
			.from(member)
			.fetch();

		for (UserDto userDto : result) {
			System.out.println("userDto = " + userDto);	
		}
	}

	@Test
	@DisplayName("Querydsl을 사용하고 생성자 방식의 Dto 조회")
	void findUserDtoByQuerydslConstructor() {
		List<UserDto> result = queryFactory
			.select(Projections.constructor(UserDto.class,
				member.name, member.age))
			.from(member)
			.fetch();

		for (UserDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@Test
	@DisplayName("QueryProjection")
	void findMemberDtoByQueryProjection() {
		// QueryProjection의 장점
		// 컴파일 시점에 타입 오류를 체크할 수 있다. (기존 생성자/필드/프로퍼티 방식의 프로젝션은 타입오류가 런타임에 발생한다.)

		// QueryProjection의 단점
		// 클래스(ex. Dto)가 Querydsl에 의존하게된다. 따라서 순수한 Dto를 원하는경우에는 사용할 수 없다.
		// compileQuerydsl 커맨드를 통해 QType 클래스를 생성해야 한다.
		List<MemberDto> result = queryFactory
			.select(new QMemberDto(member.name, member.age)).distinct()
			.from(member)
			.where(member.age.lt(30))
			.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@Test
	@DisplayName("동적쿼리 by BooleanBuilder")
	void dynamicQueryByBooleanBuilder() {
		//String name = "member2";
		String name = null;		// null과 empty는 다르니까 주의
		Integer age = 20;

		List<Member> result = searchMember(name, age);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember(String nameCondition, Integer ageCondition) {
		BooleanBuilder builder = new BooleanBuilder();
		if (nameCondition != null) {
			builder.and(member.name.eq(nameCondition));
		}
		if (ageCondition != null) {
			builder.and(member.age.eq(ageCondition));
		}

		return queryFactory
			.selectFrom(member)
			.where(builder)
			.fetch();
	}

	@Test
	@DisplayName("동적쿼리 by Where Multi Parameters")
	void dynamicQueryByWhereMultiParameters() {
		// where multi-parameters의 장점은 각각 조건들을 메서드로 구현할 수 있다는 것이다.
		// 조건들이 메서드화 되었으므로 다른 기능에서 같은 조건을 필요로 할 경우 재사용할 수 있는 장점이 있다.
		// 또한 BooleanExpressions로 반환되는 메서드는 조립도 가능한 장점이 있다.

		String nameCondition = "member3";
		//String nameCondition = null;
		Integer ageCondition = 30;

		List<Member> members = searchMember2(nameCondition, ageCondition);
		assertThat(members.size()).isEqualTo(1);
	}

	private List<Member> searchMember2(String nameCondition, Integer ageCondition) {
		List<Member> result = queryFactory
			.selectFrom(member)
			//.where(equalsName(nameCondition), equalsAge(ageCondition))
			.where(equalsAll(nameCondition, ageCondition))
			.fetch();
		return result;
	}

	private BooleanExpression equalsName(String nameCondition) {
		return nameCondition != null ? member.name.eq(nameCondition) : null;
	}

	private BooleanExpression equalsAge(Integer ageCondition) {
		return ageCondition != null ? member.age.eq(ageCondition) : null;
	}

	private Predicate equalsAll(String nameCondition, Integer ageCondition) {
		// TODO equalsName, equalsAge에서 반환되는 null에 대한 처리가 필요하다
		return equalsName(nameCondition).and(equalsAge(ageCondition));
	}
}
