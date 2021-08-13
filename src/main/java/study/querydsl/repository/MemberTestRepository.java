package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.Member;
import study.querydsl.dto.querydsl.MemberSearchCondition;
import study.querydsl.dto.querydsl.MemberTeamDto;
import study.querydsl.dto.querydsl.QMemberTeamDto;
import study.querydsl.repository.support.Querydsl4RepositorySupport;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {
	public MemberTestRepository() {
		super(Member.class);
	}

	public List<Member> basicSelect() {
		return select(member)
			.from(member)
			.fetch();
	}

	public List<Member> basicSelectFrom() {
		return selectFrom(member).fetch();
	}

	public Page<Member> searchPageByApplyPage(MemberSearchCondition condition, Pageable pageable) {
		JPAQuery<Member> jpaQuery = selectFrom(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			);

		List<Member> content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();

		return PageableExecutionUtils.getPage(content, pageable, () -> jpaQuery.fetchCount());
	}

	public Page<MemberTeamDto> applyPagination(MemberSearchCondition condition, Pageable pageable) {
		return applyPagination(pageable, contentQuery ->
			select(new QMemberTeamDto(
				member.id.as("member_id"), member.name.as("member_name"), member.age.as("member_age"),
				team.id.as("team_id"), team.name.as("team_name")))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			)
		);
	}

	public Page<MemberTeamDto> applyPagination2(MemberSearchCondition condition, Pageable pageable) {
		return applyPagination(pageable, contentQuery ->
			select(new QMemberTeamDto(
				member.id.as("member_id"), member.name.as("member_name"), member.age.as("member_age"),
				team.id.as("team_id"), team.name.as("team_name")))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			), countQuery ->
			select(member.id)
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			)
		);
	}

	private BooleanExpression equalsMemberName(String memberName) {
		return hasText(memberName) ? member.name.eq(memberName) : null;
	}

	private BooleanExpression equalsTeamName(String teamName) {
		return hasText(teamName) ? team.name.eq(teamName) : null;
	}

	private BooleanExpression betweenAge(Integer ageGoe, Integer ageLoe) {
		if (ageGoe == null) ageGoe = 0;
		if (ageLoe == null) ageLoe = 100;
		return member.age.between(ageGoe, ageLoe);
	}
}
