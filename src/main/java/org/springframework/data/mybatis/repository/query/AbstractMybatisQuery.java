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

package org.springframework.data.mybatis.repository.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mybatis.annotations.Statement;
import org.springframework.data.mybatis.annotations.Statement.Type;
import org.springframework.data.mybatis.repository.dialect.Dialect;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.CollectionExecution;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.DeleteExecution;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.ExistsExecution;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.InsertExecution;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.PagedExecution;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.SingleEntityExecution;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.SlicedExecution;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.StreamExecution;
import org.springframework.data.mybatis.repository.query.MybatisQueryExecution.UpdateExecution;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static org.springframework.data.mybatis.annotations.Statement.Type.*;

/**
 * abstract mybatis query.
 *
 * @author Jarvis Song
 */
public abstract class AbstractMybatisQuery implements RepositoryQuery {

	protected final SqlSessionTemplate sqlSessionTemplate;

	protected final MybatisQueryMethod method;

	protected final Dialect dialect;
	
	protected AbstractMybatisQuery(SqlSessionTemplate sqlSessionTemplate, MybatisQueryMethod method, Dialect dialect) {
		this.sqlSessionTemplate = sqlSessionTemplate;
		this.method = method;
		this.dialect = dialect;
	}

	@Override
	public Object execute(Object[] parameters) {
		return doExecute(getExecution(), parameters);
	}

	@Override
	public MybatisQueryMethod getQueryMethod() {
		return method;
	}

	private Object doExecute(MybatisQueryExecution execution, Object[] parameters) {

		Object result = execution.execute(this, parameters);

		ParametersParameterAccessor accessor = new ParametersParameterAccessor(method.getParameters(), parameters);
		ResultProcessor withDynamicProjection = method.getResultProcessor().withDynamicProjection(accessor);

		return withDynamicProjection.processResult(result, new TupleConverter(withDynamicProjection.getReturnedType()));
	}
	
	protected boolean isNativeStatement() {
		return null!= method.getStatementAnnotation();
	}

	protected Type getStatementType() {
		Statement annotation = method.getStatementAnnotation();
		if (null == annotation) {
			return null;
		}
		return annotation.type();
	}

	protected String getStatementId() {
		return getNamespace() + "." + getStatementName();
	}

	protected String getCountStatementId() {
		return getNamespace() + ".count_" + getStatementName();
	}

	protected String getQueryForDeleteStatementId() {
		return getNamespace() + ".query_" + getStatementName();
	}

	protected String getNamespace() {
		Statement annotation = method.getStatementAnnotation();
		if (null == annotation || StringUtils.isEmpty(annotation.namespace())) {
			return method.getEntityInformation().getJavaType().getName();
		}

		return annotation.namespace();
	}
	
	
	protected Dialect getDialect() {
		return dialect;
	}

	protected String getStatementId(String id) {
		return getNamespace() + "." + id;
	}

	protected String getStatementName() {
		Statement annotation = method.getStatementAnnotation();
		if (null == annotation || StringUtils.isEmpty(annotation.value())) {
			return method.getName();
		}
		return annotation.value();
	}

	public SqlSessionTemplate getSqlSessionTemplate() {
		return sqlSessionTemplate;
	}

	protected MybatisQueryExecution getExecution() {
		Type operation = getStatementType();
				
		if (null != operation && operation != AUTO) {
			switch (operation) {
				case INSERT:
					return new InsertExecution();
				case UPDATE:
					return new UpdateExecution();
				case SELECT_ONE:
					return new SingleEntityExecution();
				case SELECT_LIST:
					return new CollectionExecution();
				case DELETE:
					return new DeleteExecution();
				case PAGE:
					return new PagedExecution();
				case STREAM:
					return new StreamExecution();
				case SLICE:
					return new SlicedExecution();
				case AUTO:
					break;
			}
		}

		if (method.isStreamQuery()) {
			return new StreamExecution();
		}
		else if (method.isSliceQuery()) {
			return new SlicedExecution();
		}
		else if (method.isPageQuery()) {
			return new PagedExecution();
		}
		else if (method.isCollectionQuery()) {
			return new CollectionExecution();
		}
		else {
			PartTree pt = new PartTree(method.getName(), method.getEntityInformation().getJavaType());
			if (pt.isExistsProjection()) {
				return new ExistsExecution();
			}
			else {
				return new SingleEntityExecution();
			}
		}
	}

	static class TupleConverter implements Converter<Object, Object> {

		private final ReturnedType type;

		/**
		 * Creates a new {@link TupleConverter} for the given {@link ReturnedType}.
		 *
		 * @param type must not be {@literal null}.
		 */
		public TupleConverter(ReturnedType type) {

			Assert.notNull(type, "Returned type must not be null!");

			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
		 */
		@Override
		public Object convert(Object source) {

			if (!(source instanceof Tuple)) {
				return source;
			}

			Tuple tuple = (Tuple) source;
			List<TupleElement<?>> elements = tuple.getElements();

			if (elements.size() == 1) {

				Object value = tuple.get(elements.get(0));

				if (type.isInstance(value) || value == null) {
					return value;
				}
			}

			return new TupleBackedMap(tuple);
		}

		/**
		 * A {@link Map} implementation which delegates all calls to a {@link Tuple}. Depending on the provided
		 * {@link Tuple} implementation it might return the same value for various keys of which only one will appear in the
		 * key/entry set.
		 *
		 * @author Jens Schauder
		 */
		private static class TupleBackedMap implements Map<String, Object> {

			private static final String UNMODIFIABLE_MESSAGE = "A TupleBackedMap cannot be modified.";

			private final Tuple tuple;

			TupleBackedMap(Tuple tuple) {
				this.tuple = tuple;
			}

			@Override
			public int size() {
				return tuple.getElements().size();
			}

			@Override
			public boolean isEmpty() {
				return tuple.getElements().isEmpty();
			}

			/**
			 * If the key is not a {@code String} or not a key of the backing {@link Tuple} this returns {@code false}.
			 * Otherwise this returns {@code true} even when the value from the backing {@code Tuple} is {@code null}.
			 *
			 * @param key the key for which to get the value from the map.
			 * @return wether the key is an element of the backing tuple.
			 */
			@Override
			public boolean containsKey(Object key) {

				try {
					tuple.get((String) key);
					return true;
				}
				catch (IllegalArgumentException e) {
					return false;
				}
			}

			@Override
			public boolean containsValue(Object value) {
				return Arrays.stream(tuple.toArray()).anyMatch(v -> v.equals(value));
			}

			/**
			 * If the key is not a {@code String} or not a key of the backing {@link Tuple} this returns {@code null}.
			 * Otherwise the value from the backing {@code Tuple} is returned, which also might be {@code null}.
			 *
			 * @param key the key for which to get the value from the map.
			 * @return the value of the backing {@link Tuple} for that key or {@code null}.
			 */
			@Override
			@Nullable
			public Object get(Object key) {

				if (!(key instanceof String)) {
					return null;
				}

				try {
					return tuple.get((String) key);
				}
				catch (IllegalArgumentException e) {
					return null;
				}
			}

			@Override
			public Object put(String key, Object value) {
				throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
			}

			@Override
			public Object remove(Object key) {
				throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
			}

			@Override
			public void putAll(Map<? extends String, ?> m) {
				throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
			}

			@Override
			public Set<String> keySet() {

				return tuple.getElements().stream() //
						.map(TupleElement::getAlias) //
						.collect(Collectors.toSet());
			}

			@Override
			public Collection<Object> values() {
				return Arrays.asList(tuple.toArray());
			}

			@Override
			public Set<Entry<String, Object>> entrySet() {

				return tuple.getElements().stream() //
						.map(e -> new HashMap.SimpleEntry<String, Object>(e.getAlias(), tuple.get(e))) //
						.collect(Collectors.toSet());
			}
		}
	}
}
