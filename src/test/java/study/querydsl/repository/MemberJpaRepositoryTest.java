package study.querydsl.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {
	@Autowired
	private MemberJpaRepository memberJpaRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	@DisplayName("JPA Basic TEST")
	void jpaBasicTest() {
		Member member = new Member("member1");
		Member member2 = new Member("member2");

		memberJpaRepository.save(member);
		memberJpaRepository.save(member2);

		Member findMember = memberJpaRepository.findById(member.getId()).orElseThrow(() -> new IllegalArgumentException("WrongId: " + member.getId()));
		assertThat(findMember).isSameAs(member);

		List<Member> findAll = memberJpaRepository.findAll();
		assertThat(findAll.size()).isEqualTo(2);

		List<Member> findMember2 = memberJpaRepository.findByName("member2");
		assertThat(findMember2.size()).isEqualTo(1);
		assertThat(findMember2.get(0)).isSameAs(member2);
	}

	@Test
	@DisplayName("Querydsl Basic TEST")
	void querydslBasicTest() {
		Member member = new Member("member1");
		Member member2 = new Member("member2");

		memberJpaRepository.save(member);
		memberJpaRepository.save(member2);

		Member findMember = memberJpaRepository.findByIdWithQuerydsl(member.getId()).orElseThrow(() -> new IllegalArgumentException("WrongId: " + member.getId()));
		assertThat(findMember).isSameAs(member);

		List<Member> findAll = memberJpaRepository.findAllWithQuerydsl();
		assertThat(findAll.size()).isEqualTo(2);

		List<Member> findMember2 = memberJpaRepository.findByNameWithQuerydsl("member2");
		assertThat(findMember2.size()).isEqualTo(1);
		assertThat(findMember2.get(0)).isSameAs(member2);
	}

}