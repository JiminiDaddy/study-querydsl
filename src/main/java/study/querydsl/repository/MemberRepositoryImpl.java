package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import study.querydsl.domain.Member;
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

	@Override
	public Page<MemberTeamDto> searchPagingSimple(MemberSearchCondition condition, Pageable pagable) {
		// fetchResults는 content와 total을 구하기위해 2번의 Query를 DB로 전송하므로, 경우에 따라 최적화가 필요할 수 있다
		QueryResults<MemberTeamDto> result = jpaQueryFactory
			.select(new QMemberTeamDto(
				member.id.as("member_id"), member.name.as("member_name"), member.age.as("member_age"),
				team.id.as("team_id"), team.name.as("team_name")))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			)
			.offset(pagable.getOffset())
			.limit(pagable.getPageSize())
			.fetchResults();

		long total = result.getTotal();
		List<MemberTeamDto> content = result.getResults();
		return new PageImpl<>(content, pagable, total);
	}

	@Override
	public Page<MemberTeamDto> searchPagingComplex(MemberSearchCondition condition, Pageable pagable) {
		// total을 구하는데 페이징조건은 필요없으므로 제외한다
		long total = jpaQueryFactory
			.select(member)
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			)
			.fetchCount();

		// total이 0인경우는 아래 Query는 실행할 필요 없으므로, 최적화가 가능하다
		if (total == 0) {
			return Page.empty();
		}

		List<MemberTeamDto> content = jpaQueryFactory
			.select(new QMemberTeamDto(
				member.id.as("member_id"), member.name.as("member_name"), member.age.as("member_age"),
				team.id.as("team_id"), team.name.as("team_name")
			))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			)
			.offset(pagable.getOffset())
			.limit(pagable.getPageSize())
			.fetch();

		return new PageImpl<>(content, pagable, total);
	}

	@Override
	public Page<MemberTeamDto> searchPagingComplexBySpringData(MemberSearchCondition condition, Pageable pagable) {
		// Spring-Data가 제공해주는 Count Query 최적화 기능 사용
		List<MemberTeamDto> content = jpaQueryFactory
			.select(new QMemberTeamDto(
				member.id.as("member_id"), member.name.as("member_name"), member.age.as("member_age"),
				team.id.as("team_id"), team.name.as("team_name")
			))
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			)
			.offset(pagable.getOffset())
			.limit(pagable.getPageSize())
			.fetch();

		JPAQuery<Member> countQuery = jpaQueryFactory
			.select(member)
			.from(member)
			.leftJoin(member.team, team)
			.where(
				equalsMemberName(condition.getMemberName()),
				equalsTeamName(condition.getTeamName()),
				betweenAge(condition.getAgeGoe(), condition.getAgeLoe())
			);

		// count query가 필요한 경우에만 3번째인자인 Supplier가 실행된다.
		// count 수가 paging수보다 작은경우, 마지막 페이지인경우, count query가 생략된다. (Spring-Data가 제공해줌)
		return PageableExecutionUtils.getPage(content, pagable, countQuery::fetchCount);
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
