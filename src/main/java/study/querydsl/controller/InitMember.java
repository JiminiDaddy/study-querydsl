package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.Team;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")
@RequiredArgsConstructor
@Component
public class InitMember {
	private final InitMemberService initMemberService;

	// Spring LifeCycle에 의해 PostConstruct와 Transactional이 동시에 일어날 수 없기때문에
	// 내부 클래스로 구현하여 빈이 생성된 뒤, 트랜잭션을 호출한다.
	@PostConstruct
	public void init() {
		initMemberService.init();
	}

	@Component
	static class InitMemberService {
		@PersistenceContext
		private EntityManager entityManager;


		@Transactional
		public void init() {
			Team teamA = new Team("teamA");
			Team teamB = new Team("teamB");
			entityManager.persist(teamA);
			entityManager.persist(teamB);

			for (int i = 1; i <= 100; ++i) {
				Team selectedTeam = i % 2 == 0 ? teamA : teamB;
				entityManager.persist(new Member("member" + i, i, selectedTeam));
			}
		}
	}
}
