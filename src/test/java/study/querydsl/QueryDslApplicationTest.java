package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Hello;
import study.querydsl.domain.QHello;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@Rollback(false)
@Transactional
@SpringBootTest
class QueryDslApplicationTest {

	// Spring 최신버전에서는 @Autowired로 대체 가능함
	// 만약 JPA를 Spring이 아닌 다른 프레임워크를 사용한다면 @PersistenceContext가 필요함
	@PersistenceContext
	private EntityManager entityManager;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		entityManager.persist(hello);

		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		//QHello qHello = new QHello("h");
		QHello qHello = QHello.hello;

		Hello result = queryFactory
			.selectFrom(qHello)
			.fetchOne();

		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}
}
