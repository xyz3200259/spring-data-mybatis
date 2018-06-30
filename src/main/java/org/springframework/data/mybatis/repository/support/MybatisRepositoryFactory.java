/*
 *
 *   Copyright 2016 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.springframework.data.mybatis.repository.support;

import java.util.Optional;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.data.mybatis.id.IdentityGeneratorFactory;
import org.springframework.data.mybatis.mapping.MybatisMappingContext;
import org.springframework.data.mybatis.repository.dialect.Dialect;
import org.springframework.data.mybatis.repository.query.MybatisQueryLookupStrategy;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.util.Assert;

import static org.springframework.data.querydsl.QuerydslUtils.*;

/**
 * mybatis implementation of repository factory.
 *
 * @author Jarvis Song
 */
public class MybatisRepositoryFactory extends RepositoryFactorySupport {

	private final SqlSessionTemplate sessionTemplate;

	private final Dialect dialect;

	private final MybatisMappingContext mappingContext;

	private IdentityGeneratorFactory<?, ?, ?> identityGeneratorFactory;

	public MybatisRepositoryFactory(final MybatisMappingContext mappingContext,
			final SqlSessionTemplate sessionTemplate, final Dialect dialect,
			final IdentityGeneratorFactory<?, ?, ?> identityGeneratorFactory) {
		Assert.notNull(sessionTemplate, "SqlSessionTemplate must not be null!");
		Assert.notNull(dialect, "Dialect must not be null!");
		this.mappingContext = mappingContext;
		this.sessionTemplate = sessionTemplate;
		this.dialect = dialect;
		this.identityGeneratorFactory = identityGeneratorFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, ID> MybatisEntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		return (MybatisEntityInformation<T, ID>) MybatisEntityInformationSupport.getEntityInformation(mappingContext,
				domainClass);
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation information) {

		// generate Mapper statements.
		new MybatisSimpleRepositoryMapperGenerator(sessionTemplate.getConfiguration(), dialect, mappingContext,
				information.getDomainType()).generate();

		return getTargetRepositoryViaReflection(information, getEntityInformation(information.getDomainType()),
				sessionTemplate, identityGeneratorFactory);

	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
		if (isQueryDslExecutor(metadata.getRepositoryInterface())) {
			throw new UnsupportedOperationException("unsupported QueryDsl Repository...");
		}
		else {
			return SimpleMybatisRepository.class;
		}
	}

	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
			EvaluationContextProvider evaluationContextProvider) {
		return Optional.of(MybatisQueryLookupStrategy.create(mappingContext, sessionTemplate, dialect, key,
				evaluationContextProvider));
	}

	private boolean isQueryDslExecutor(Class<?> repositoryInterface) {
		return QUERY_DSL_PRESENT && QuerydslPredicateExecutor.class.isAssignableFrom(repositoryInterface);
	}

}
