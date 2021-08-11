package study.querydsl.dto.querydsl;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberTeamDto {
	private Long memberId;

	private String memberName;

	private Integer memberAge;

	private Long teamId;

	private String teamName;

	// MemberTeamDto를 QClass로 빌드하여 사용한다.
	@QueryProjection
	public MemberTeamDto(Long memberId, String memberName, Integer memberAge, Long teamId, String teamName) {
		this.memberId = memberId;
		this.memberName = memberName;
		this.memberAge = memberAge;
		this.teamId = teamId;
		this.teamName = teamName;
	}
}
