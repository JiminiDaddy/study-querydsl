package study.querydsl.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@ToString(of = {"id", "name", "age"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Member {
	@Id
	@GeneratedValue
	@Column(name = "member_id")
	private Long id;

	private String name;

	private int age;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Team team;

	Member(String name) {
		this.name = name;
	}

	public Member(String name, int age) {
		this(name);
		this.age = age;
	}

	public Member(String name, int age, Team team) {
		this(name, age);
		this.team = team;
		if (this.team != null) {
			this.team.removeMember(this);
			changeTeam(team);
		}
	}

	public void changeName(String name) {
		this.name = name;
	}

	public void changeTeam(Team team) {
		this.team = team;
		this.team.addMember(this);
	}
}
