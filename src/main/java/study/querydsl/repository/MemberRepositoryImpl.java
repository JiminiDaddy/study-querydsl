package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import study.querydsl.dto.querydsl.MemberSearchCondition;
import study.querydsl.dto.querydsl.MemberTeamDto;
import study.querydsl.dto.querydsl.QMemberTeamDto;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

public class MemberRepositoryImpl implements MemberRepositoryCustom {
	private final JPAQueryFactory jpaQueryFactory;

	public MemberRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
		this.jpaQueryFactory = jpaQueryFactory;
	}

	@Override
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return jpaQueryFactory
			.select(new QMemberTeamDto(
				member.id.as("member_id"), member.name.as("member_name"), member.age.as("member_age"), team.id.as("team_id"), team.name.as("team_name")
			))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
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

	private BooleanExpression betweenAge(Integer ageGoe, Integer ageLoe) {
		return member.age.between(ageGoe, ageLoe);
	}
}
