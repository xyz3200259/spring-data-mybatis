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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.mybatis.repository.query.MybatisParameters.MybatisParameter;
import org.springframework.data.mybatis.repository.support.NativePagedQueryPlugin;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * base query execution of mybatis implementation.
 *
 * @author Jarvis Song
 */
public abstract class MybatisQueryExecution {

	private static final ConversionService CONVERSION_SERVICE;

	static {

		ConfigurableConversionService conversionService = new DefaultConversionService();

		conversionService.addConverter(MybatisResultConverters.BlobToByteArrayConverter.INSTANCE);
		conversionService.removeConvertible(Collection.class, Object.class);
		potentiallyRemoveOptionalConverter(conversionService);

		CONVERSION_SERVICE = conversionService;
	}

	protected abstract Object doExecute(AbstractMybatisQuery query, Object[] values);

	public Object execute(AbstractMybatisQuery query, Object[] values) {

		Assert.notNull(query, "query can not be null");
		Assert.notNull(values, "values can not be null");

		Object result;

		try {
			result = doExecute(query, values);
		}
		catch (NoResultException e) {
			return null;
		}

		if (result == null) {
			return null;
		}

		MybatisQueryMethod queryMethod = query.getQueryMethod();
		Class<?> requiredType = queryMethod.getReturnType();

		if (void.class.equals(requiredType) || requiredType.isAssignableFrom(result.getClass())) {
			return result;
		}

		return CONVERSION_SERVICE.canConvert(result.getClass(), requiredType)
				? CONVERSION_SERVICE.convert(result, requiredType)
				: result;
	}

	static class ExistsExecution extends MybatisQueryExecution {

		@Override
		protected Object doExecute(AbstractMybatisQuery query, Object[] values) {

			if (null == values || values.length == 0) {
				return query.getSqlSessionTemplate().selectList(query.getStatementId());
			}

			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();

			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			if (parameters.hasSortParameter()) {
				parameter.put("_sorts", values[parameters.getSortIndex()]);
			}

			List<?> result = query.getSqlSessionTemplate().selectList(query.getStatementId(), parameter);
			return result.size() > 0;

		}
	}

	static class CollectionExecution extends MybatisQueryExecution {

		@Override
		protected Object doExecute(AbstractMybatisQuery query, Object[] values) {

			if (null == values || values.length == 0) {
				return query.getSqlSessionTemplate().selectList(query.getStatementId());
			}

			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();

			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			if (parameters.hasSortParameter()) {
				parameter.put("_sorts", values[parameters.getSortIndex()]);
			}

			return query.getSqlSessionTemplate().selectList(query.getStatementId(), parameter);

		}
	}

	static class SlicedExecution extends MybatisQueryExecution {

		@Override
		protected Object doExecute(AbstractMybatisQuery query, Object[] values) {
			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();
			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			Pageable pageable = (Pageable) values[parameters.getPageableIndex()];
			if (parameters.hasSortParameter()) {
				parameter.put("_sorts", values[parameters.getSortIndex()]);
			}
			else {
				if (pageable.getSort() != null && pageable.getSort().isSorted()) {
					parameter.put("_sorts", pageable.getSort());
				}
			}
			parameter.put("offset", pageable.getOffset());
			parameter.put("pageSize", pageable.getPageSize() + 1);
			parameter.put("offsetEnd", pageable.getOffset() + pageable.getPageSize());
			List<Object> resultList = query.getSqlSessionTemplate().selectList(query.getStatementId(), parameter);

			int pageSize = pageable.getPageSize();
			boolean hasNext = resultList.size() > pageSize;

			return new SliceImpl<Object>(hasNext ? resultList.subList(0, pageSize) : resultList, pageable, hasNext);
		}
	}

	static class PagedExecution extends MybatisQueryExecution {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected Object doExecute(AbstractMybatisQuery query, Object[] values) {
			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();
			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			Pageable pager = (Pageable) values[parameters.getPageableIndex()];

			if (parameters.hasSortParameter()) {
				parameter.put("_sorts", values[parameters.getSortIndex()]);
			}
			else if (null != pager) {
				if (pager.getSort() != null && pager.getSort().isSorted()) {
					parameter.put("_sorts", pager.getSort());
				}
			}

			parameter.put("offset", null == pager ? 0 : pager.getOffset());
			parameter.put("pageSize", null == pager ? Integer.MAX_VALUE : pager.getPageSize());
			parameter.put("offsetEnd", null == pager ? Integer.MAX_VALUE : pager.getOffset() + pager.getPageSize());

			List<?> result = new ArrayList<>();
			try {
				if (query.isNativeStatement()) {
					NativePagedQueryPlugin.startNativePagedQuery(query.getDialect());
				}
				result = query.getSqlSessionTemplate().selectList(query.getStatementId(), parameter);
			}
			finally {
				if (query.isNativeStatement()) {
					NativePagedQueryPlugin.endNativePagedQuery();
				}
			}

			if (null == pager) {
				return new PageImpl(result);
			}

			long total = calculateTotal(pager, result);
			if (total < 0) {
				if(isCountStatementIdExist(query)) {
					total = query.getSqlSessionTemplate().selectOne(query.getCountStatementId(), parameter);
				} else {
					if (query.isNativeStatement()) {
						NativePagedQueryPlugin.startNativePagedCountQuery();
						try {
							query.getSqlSessionTemplate().clearCache();
							total = query.getSqlSessionTemplate().selectOne(query.getStatementId(), parameter);
						}
						finally {
							NativePagedQueryPlugin.endNativePagedCountQuery();
						}
					}
				}
			}
			return new PageImpl(result, pager, total);
		}
		
		private boolean isCountStatementIdExist(AbstractMybatisQuery query) {
			try {
			query.getSqlSessionTemplate().getConfiguration().getMappedStatement(query.getCountStatementId());
			return true;
			}catch(IllegalArgumentException e) {
				return false;
			}
		}
		

		protected <X> long calculateTotal(Pageable pager, List<X> result) {
			if (pager.hasPrevious()) {
				if (CollectionUtils.isEmpty(result))
					return -1;
				if (result.size() == pager.getPageSize())
					return -1;
				return (pager.getPageNumber() - 1) * pager.getPageSize() + result.size();
			}
			if (result.size() < pager.getPageSize())
				return result.size();
			return -1;
		}
	}

	static class InsertExecution extends MybatisQueryExecution {

		@Override
		protected Object doExecute(AbstractMybatisQuery query, Object[] values) {
			if (null == values || values.length == 0) {
				return query.getSqlSessionTemplate().insert(query.getStatementId());
			}
			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();

			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			return query.getSqlSessionTemplate().insert(query.getStatementId(), parameter);
		}
	}

	static class UpdateExecution extends MybatisQueryExecution {

		@Override
		protected Object doExecute(AbstractMybatisQuery query, Object[] values) {
			if (null == values || values.length == 0) {
				return query.getSqlSessionTemplate().update(query.getStatementId());
			}
			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();

			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			return query.getSqlSessionTemplate().update(query.getStatementId(), parameter);
		}
	}

	static class SingleEntityExecution extends MybatisQueryExecution {

		@Override
		protected Object doExecute(final AbstractMybatisQuery query, final Object[] values) {
			if (null == values || values.length == 0) {
				return query.getSqlSessionTemplate().selectOne(query.getStatementId());
			}
			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();

			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			return query.getSqlSessionTemplate().selectOne(query.getStatementId(), parameter);
		}
	}

	static class StreamExecution extends MybatisQueryExecution {

		@Override
		protected Object doExecute(AbstractMybatisQuery query, Object[] values) {

			if (null == values || values.length == 0) {
				return query.getSqlSessionTemplate().selectList(query.getStatementId());
			}

			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();

			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			if (parameters.hasSortParameter()) {
				parameter.put("_sorts", values[parameters.getSortIndex()]);
			}

			return query.getSqlSessionTemplate().selectList(query.getStatementId(), parameter);

		}

	}

	static class DeleteExecution extends MybatisQueryExecution {

		@Override
		protected Object doExecute(AbstractMybatisQuery query, Object[] values) {
			MybatisParameters parameters = query.getQueryMethod().getParameters();
			Map<String, Object> parameter = new HashMap<String, Object>();

			int c = 0;
			for (MybatisParameter param : parameters.getBindableParameters()) {
				String name = param.getName().orElse("p" + c);
				parameter.put(name, values[param.getIndex()]);
				c++;
			}

			boolean collectionQuery = query.getQueryMethod().isCollectionQuery();
			Object result = null;
			if (collectionQuery) {
				result = query.getSqlSessionTemplate().selectList(query.getQueryForDeleteStatementId(), parameter);
			}

			int rows = query.getSqlSessionTemplate().delete(query.getStatementId(), parameter);
			if (!collectionQuery) {
				return rows;
			}
			return result;
		}
	}

	public static void potentiallyRemoveOptionalConverter(ConfigurableConversionService conversionService) {

		ClassLoader classLoader = MybatisQueryExecution.class.getClassLoader();

		if (ClassUtils.isPresent("java.util.Optional", classLoader)) {

			try {

				Class<?> optionalType = ClassUtils.forName("java.util.Optional", classLoader);
				conversionService.removeConvertible(Object.class, optionalType);

			}
			catch (ClassNotFoundException e) {
				return;
			}
			catch (LinkageError e) {
				return;
			}
		}
	}
}
