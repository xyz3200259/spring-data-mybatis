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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.GeneratedValue;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyHandler;
import org.springframework.data.mybatis.id.IdentityGenerator;
import org.springframework.data.mybatis.id.IdentityGeneratorFactory;
import org.springframework.data.mybatis.mapping.MybatisPersistentProperty;
import org.springframework.data.mybatis.repository.domain.ExampleInfo;
import org.springframework.data.support.ExampleMatcherAccessor;
import org.springframework.data.util.DirectFieldAccessFallbackBeanWrapper;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * Default implementation of the {@link org.springframework.data.repository.CrudRepository}
 * interface.
 *
 * @author Jarvis Song
 */
@Repository
public class SimpleMybatisRepository<T, ID extends Serializable> extends SqlSessionRepositorySupport
		implements MybatisRepository<T, ID> {

	private static final String STATEMENT_INSERT = "_insert";

	private static final String STATEMENT_UPDATE = "_update";

	private static final String STATEMENT_UPDATE_IGNORE_NULL = "_updateIgnoreNull";

	private static final String STATEMENT_GET_BY_ID = "_getById";

	private static final String STATEMENT_DELETE_BY_ID = "_deleteById";

	private TypeConverter idConverter = new SimpleTypeConverter();

	private final MybatisEntityInformation<T, ID> entityInformation;

	private IdentityGeneratorFactory<ID, T, MybatisPersistentProperty> identityGeneratorFactory;

	public SimpleMybatisRepository(MybatisEntityInformation<T, ID> entityInformation,
			SqlSessionTemplate sqlSessionTemplate,
			IdentityGeneratorFactory<ID, T, MybatisPersistentProperty> identityGeneratorFactory) {
		super(sqlSessionTemplate);
		this.entityInformation = entityInformation;
		this.identityGeneratorFactory = identityGeneratorFactory;
	}

	@Override
	protected String getNamespace() {
		return entityInformation.getJavaType().getName();
	}

	@Override
	public <S extends T> S insert(S entity) {
		insert(STATEMENT_INSERT, entity);
		return entity;
	}

	@Override
	public <S extends T> S update(S entity) {
		int row = update(STATEMENT_UPDATE, entity);
		if (row == 0) {
			throw new MybatisNoHintException("update effect 0 row, maybe version control lock occurred.");
		}
		return entity;
	}

	@Override
	public <S extends T> S updateIgnoreNull(S entity) {
		int row = update(STATEMENT_UPDATE_IGNORE_NULL, entity);
		if (row == 0) {
			throw new MybatisNoHintException("update effect 0 row, maybe version control lock occurred.");
		}
		return entity;
	}

	@Override
	public <S extends T> S save(S entity) {
		Assert.notNull(entity, "entity can not be null");
		if (entityInformation.isNew(entity)) {
			// Process GeneratedValue
			entityInformation.getPersistentEntity().doWithProperties(
					(PropertyHandler<MybatisPersistentProperty>) (p) -> {
						if (p.isIdProperty()) {
							GeneratedValue gv = p.findAnnotation(GeneratedValue.class);
							if (null != gv) {
								IdentityGenerator<ID> generator = identityGeneratorFactory.resolve(gv.strategy(),
										gv.generator(), entityInformation.getPersistentEntity());
								Assert.notNull(generator,
										String.format("No suitable IdentityGenerator foud for Entity %s Property %s",
												entityInformation.getEntityName(), p.getName()));
								ID id = generator.generate(getSqlSession(), p);
								ReflectionUtils.setField(p.getField(), entity,
										idConverter.convertIfNecessary(id, p.getActualType()));
							}
						}
					});
			insert(entity);
		}
		else {
			// update
			update(entity);
		}
		return entity;
	}

	@Override
	public <S extends T> S saveIgnoreNull(S entity) {
		Assert.notNull(entity, "entity can not be null");

		if (entityInformation.isNew(entity)) {
			// insert
			insert(entity);
		}
		else {
			// update
			updateIgnoreNull(entity);
		}
		return entity;
	}

	@Override
	public Optional<T> findById(ID id) {
		Assert.notNull(id, "id can not be null");
		return selectOne(STATEMENT_GET_BY_ID, id);
	}

	@Override
	public boolean existsById(ID id) {
		return findById(id).isPresent();
	}

	@Override
	public long count() {
		return selectOne("_count");
	}

	@Override
	public void deleteById(ID id) {
		Assert.notNull(id, "id can not be null");
		super.delete(STATEMENT_DELETE_BY_ID, id);
	}

	@Override
	public void delete(T entity) {
		Assert.notNull(entity, "entity can not be null!");
		deleteById(entityInformation.getId(entity));
	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {
		if (null == entities) {
			return;
		}
		for (T entity : entities) {
			delete(entity);
		}
	}

	@Override
	public void deleteAll() {
		super.delete("_deleteAll");
	}

	@Override
	public List<T> findAll() {
		return findAll((Sort) null);
	}

	@Override
	public List<T> findAll(Sort sort) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("_sorts", sort);
		return selectList("_findAll", params);
	}

	@Override
	public List<T> findAllById(Iterable<ID> ids) {
		if (null == ids) {
			return Collections.emptyList();
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("_ids", ids);
		return selectList("_findAll", params);
	}

	@Override
	public <S extends T> List<S> saveAll(Iterable<S> entities) {
		if (null == entities) {
			return Collections.emptyList();
		}
		for (S entity : entities) {
			save(entity);
		}
		return (List<S>) entities;
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example) {
		Assert.notNull(example, "Example can not be null!");
		Map<String, Object> params = new HashMap<>();
		params.put("_example", buildExample(example));
		return selectList("_findAll", params);
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("_sorts", sort);
		params.put("_example", buildExample(example));
		return selectList("_findAll", params);
	}

	@Override
	public <S extends T> Optional<S> findOne(Example<S> example) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("_example", buildExample(example));
		return Optional.of(selectOne("_findAll", params));
	}

	@Override
	public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
		return findByPager(pageable, "_findByPager", "_countByExample", buildExample(example));
	}

	@Override
	public <S extends T> long count(Example<S> example) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("_example", buildExample(example));
		return selectOne("_countByExample", params);
	}

	@Override
	public <S extends T> boolean exists(Example<S> example) {
		return count(example)!=0;
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		if (null == pageable) {
			return new PageImpl<T>(findAll());
		}
		return findByPager(pageable, "_findByPager", "_countByExample", null);
	}

	@Override
	public void deleteInBatch(Iterable<T> entities) {
		//TODO improve delete in batch
		deleteAll(entities);
	}

	private <S extends T> Map<String, ExampleInfo> buildExample(Example<S> example) {
		Map<String, ExampleInfo> theExample = new HashMap<>();
		ExampleMatcher matcher = example.getMatcher();
		ExampleMatcherAccessor accesser = new ExampleMatcherAccessor(matcher);
		DirectFieldAccessFallbackBeanWrapper beanWrapper = new DirectFieldAccessFallbackBeanWrapper(example.getProbe());
		entityInformation.getPersistentEntity().doWithProperties((PropertyHandler<MybatisPersistentProperty>) (p) -> {
			if (!p.isIdProperty()) {
				ExampleInfo info = new ExampleInfo();
				Object value = beanWrapper.getPropertyValue(p.getName());

				if(accesser.isIgnoredPath(p.getName())) {
					return;
				}
				
				if (null == value) {
					if (matcher.getNullHandler().equals(ExampleMatcher.NullHandler.INCLUDE)) {
						info.setIncludeNull(true);
						theExample.put(p.getName(), info);
					}
					return;
				}
				if (p.getActualType().equals(String.class)) {
					if (accesser.isIgnoreCaseForPath(p.getName())) {
						info.setIgnoreCase(true);
						info.setValue(((String) value).toUpperCase());
					}else {
						info.setValue(value);
					}
					switch (accesser.getStringMatcherForPath(p.getName())) {
						case DEFAULT:
						case EXACT:
							info.setMatcher(StringMatcher.EXACT.toString());
							break;
						case CONTAINING:
							info.setMatcher(StringMatcher.CONTAINING.toString());
							break;
						case STARTING:
							info.setMatcher(StringMatcher.STARTING.toString());
							break;
						case ENDING:
							info.setMatcher(StringMatcher.ENDING.toString());
							break;
						default:
							throw new IllegalArgumentException(
									"Unsupported StringMatcher " + accesser.getStringMatcherForPath(p.getName()));
					}

				}
				else {
					info.setMatcher(StringMatcher.EXACT.toString());
					info.setValue(value);
				}

				theExample.put(p.getName(), info);
			}
		});
		return theExample;
	}

}
