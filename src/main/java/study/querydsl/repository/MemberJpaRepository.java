package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {
	private final EntityManager entityManager;

	private final JPAQueryFactory jpaQueryFactory;

	public MemberJpaRepository(EntityManager entityManager, JPAQueryFactory jpaQueryFactory) {
		this.entityManager = entityManager;
		this.jpaQueryFactory  = jpaQueryFactory;
	}

	public void save(Member member) {
		entityManager.persist(member);
	}

	public Optional<Member> findById(Long id) {
		return Optional.ofNullable(entityManager.find(Member.class, id));
	}

	public List<Member> findAll() {
		return entityManager.createQuery("select m from Member m")
			.getResultList();
	}

	public List<Member> findByName(String name) {
		return entityManager.createQuery("select m from Member m where m.name = :name")
			.setParameter("name", name)
			.getResultList();
	}

	public Optional<Member> findByIdWithQuerydsl(Long id) {
		return Optional.ofNullable(jpaQueryFactory
			.selectFrom(QMember.member)
			.where(QMember.member.id.eq(id))
			.fetchOne());
	}

	public List<Member> findAllWithQuerydsl() {
		return jpaQueryFactory
			.selectFrom(QMember.member)
			.fetch();
	}

	public List<Member> findByNameWithQuerydsl(String name) {
		return jpaQueryFactory
			.selectFrom(QMember.member)
			.where(QMember.member.name.eq(name))
			.fetch();
	}
}
