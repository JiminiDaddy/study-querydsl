package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Data
public class MemberDto {
	private String name;
	private int age;

	@QueryProjection
	public MemberDto(String name, int age) {
		this.name = name;
		this.age = age;
	}
}
