package study.querydsl.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import study.querydsl.dto.querydsl.MemberSearchCondition;
import study.querydsl.dto.querydsl.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
	List<MemberTeamDto> search(MemberSearchCondition condition);

	Page<MemberTeamDto> searchPagingSimple(MemberSearchCondition condition, Pageable pagable);

	Page<MemberTeamDto> searchPagingComplex(MemberSearchCondition condition, Pageable pagable);

	Page<MemberTeamDto> searchPagingComplexBySpringData(MemberSearchCondition condition, Pageable pagable);
}
