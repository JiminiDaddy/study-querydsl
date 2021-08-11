package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.querydsl.MemberSearchCondition;
import study.querydsl.dto.querydsl.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class MemberApiController {
	private final MemberJpaRepository memberJpaRepository;

	@GetMapping("/api/v1/members")
	public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
		return memberJpaRepository.searchByBuilder(condition);
	}
}
