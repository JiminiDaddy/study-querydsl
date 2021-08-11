package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.querydsl.MemberSearchCondition;
import study.querydsl.dto.querydsl.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MemberApiController {
	private final MemberJpaRepository memberJpaRepository;

	private final MemberRepository memberRepository;

	@GetMapping("/api/v1/members")
	public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
		return memberJpaRepository.searchByBuilder(condition);
	}

	@GetMapping("/api/v2/members")
	public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
		return memberRepository.searchPagingComplex(condition, pageable);
	}

	@GetMapping("/api/v3/members")
	// ex. teamname=teamB, page=2, size=20 일 경우 total query 최적화가 되어야 한다
	public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
		return memberRepository.searchPagingComplexBySpringData(condition, pageable);
	}
}
