package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.Member;
import study.querydsl.dto.querydsl.MemberSearchCondition;
import study.querydsl.dto.querydsl.MemberTeamDto;
import study.querydsl.dto.querydsl.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

@Repository
public class MemberJpaRepository {
	private final EntityManager entityManager;

	private final JPAQueryFactory jpaQueryFactory;

	public MemberJpaRepository(EntityManager entityManager, JPAQueryFactory jpaQueryFactory) {
		this.entityManager = entityManager;
		this.jpaQueryFactory  = jpaQueryFactory;
	}

	public void save(Member member) {
		entityManager.persist(member);
	}

	public Optional<Member> findById(Long id) {
		return Optional.ofNullable(entityManager.find(Member.class, id));
	}

	public List<Member> findAll() {
		return entityManager.createQuery("select m from Member m")
			.getResultList();
	}

	public List<Member> findByName(String name) {
		return entityManager.createQuery("select m from Member m where m.name = :name")
			.setParameter("name", name)
			.getResultList();
	}

	public Optional<Member> findByIdWithQuerydsl(Long id) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(member)
			.where(member.id.eq(id))
			.fetchOne());
	}

	public List<Member> findAllWithQuerydsl() {
		return jpaQueryFactory
			.selectFrom(member)
			.fetch();
	}

	public List<Member> findByNameWithQuerydsl(String name) {
		return jpaQueryFactory
			.selectFrom(member)
			.where(member.name.eq(name))
			.fetch();
	}

	public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
		BooleanBuilder builder = new BooleanBuilder();

		if (hasText(condition.getMemberName())) {
			builder.and(member.name.eq(condition.getMemberName()));
		}

		if (hasText(condition.getTeamName())) {
			builder.and(team.name.eq(condition.getTeamName()));
		}

		if (condition.getAgeGoe() != null) {
			builder.and(member.age.goe(condition.getAgeGoe()));
		}

		if (condition.getAgeLoe() != null) {
			builder.and(member.age.loe(condition.getAgeLoe()));
		}

		return jpaQueryFactory
			.select(new QMemberTeamDto(
				member.id.as("member_id"), member.name, member.age, team.id.as("team_id"), team.name))
			.from(member)
			.leftJoin(member.team, team)
			.where(builder)
			.fetch();
	}

	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return jpaQueryFactory
			.select(new QMemberTeamDto(
				member.id.as("member_id"), member.name, member.age, team.id.as("team_id"), team.name))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				//goeAge(condition.getAgeGoe()),
				//loeAge(condition.getAgeLoe())
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			)
			.fetch();
	}

	private BooleanExpression equalsMemberName(String memberName) {
		return hasText(memberName) ? member.name.eq(memberName) : null;
	}

	private BooleanExpression equalsTeamName(String teamName) {
		return hasText(teamName) ? team.name.eq(teamName) : null;
	}

	private BooleanExpression goeAge(Integer ageGoe) {
		return ageGoe != null ? member.age.goe(ageGoe) : null;
	}

	private BooleanExpression loeAge(Integer ageLoe) {
		return ageLoe != null ? member.age.loe(ageLoe) : null;
	}

	private BooleanExpression betweenAge(Integer ageGoe, Integer ageLoe) {
		if (ageGoe == null)	ageGoe = 0;		// TODO 최소치는 정책에서 정해진 값으로 설정해야함
		if (ageLoe == null) ageLoe = 100;	// TODO 최대치는 정책에서 정해진 값으로 설정해야함

		//return member.age.between(ageGoe, ageLoe);
		// 위 주석된 코드와 같이 between이 더 깔끔하지만 예제로 구현된 메서드를 조립하여 사용할 수 있다는것을 증명하기 위해 아래와 같이 구현함
		return goeAge(ageGoe).and(loeAge(ageLoe));
	}
}
