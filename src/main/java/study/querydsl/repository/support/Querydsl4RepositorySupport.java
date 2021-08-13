package study.querydsl.repository.support;

import com.mysema.commons.lang.Assert;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.function.Function;

@Repository
public abstract class Querydsl4RepositorySupport {
	private final Class domainClass;

	private Querydsl querydsl;

	private EntityManager entityManager;

	private JPAQueryFactory jpaQueryFactory;

	public Querydsl4RepositorySupport(Class<?> domainClass) {
		Assert.notNull(domainClass, "Domain class must not be null!");
		this.domainClass = domainClass;
	}

	@Autowired
	public void setEntityManager(EntityManager entityManager) {
		Assert.notNull(entityManager, "EntityManager must not be null!");
		this.entityManager = entityManager;

		JpaEntityInformation entityInformation =
			JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager);

		SimpleEntityPathResolver pathResolver = SimpleEntityPathResolver.INSTANCE;
		EntityPath path = pathResolver.createPath(entityInformation.getJavaType());

		this.querydsl = new Querydsl(entityManager, new PathBuilder<>(path.getType(), path.getMetadata()));
		this.jpaQueryFactory = new JPAQueryFactory(entityManager);
	}

	@PostConstruct
	public void validate() {
		Assert.notNull(entityManager, "EntityManager must not be null!")	;
		Assert.notNull(querydsl, "Querydsl must not be null!");
		Assert.notNull(jpaQueryFactory, "QueryFactory must not be null!");
	}

	protected JPAQueryFactory getQueryFactory() {
		return jpaQueryFactory;
	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	protected Querydsl getQuerydsl() {
		return querydsl;
	}

	protected <T> JPAQuery<T> select(Expression<T> expr) {
		return getQueryFactory().select(expr);
	}

	protected <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
		return getQueryFactory().selectFrom(from);
	}

	protected <T> Page<T> applyPagination(Pageable pageable, Function<JPAQueryFactory, JPAQuery> contentQuery) {
		// 입력받은 Query 메서드로부터 JPAQuery 객체를 가져온다
		JPAQuery jpaQuery = contentQuery.apply(getQueryFactory());
		// querydsl의 페이징 처리를 통해 content에 페이징 조건을 추가한다
		List<T> content = getQuerydsl().applyPagination(pageable, jpaQuery).fetch();
		return PageableExecutionUtils.getPage(content, pageable, () -> jpaQuery.fetchCount());
	}

	protected <T> Page<T> applyPagination(Pageable pageable,
										  Function<JPAQueryFactory, JPAQuery> contentQuery,
										  Function<JPAQueryFactory, JPAQuery> countQuery) {
		// content와 count를 분리 (count query 최적화 위함)
		JPAQuery jpqContentQuery = contentQuery.apply(getQueryFactory());
		List<T> content = getQuerydsl().applyPagination(pageable, jpqContentQuery).fetch();
		JPAQuery count = countQuery.apply(getQueryFactory());
		return PageableExecutionUtils.getPage(content, pageable, count::fetchCount);

	}

}
