/*
 * Copyright (c) 2018-present the original author or authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.springframework.data.mybatis.repository.support;

import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.data.mybatis.repository.dialect.Dialect;
import org.springframework.data.mybatis.repository.query.QueryUtils;
import org.springframework.util.ReflectionUtils;

@Intercepts({
	@Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
public class NativePagedQueryPlugin implements Interceptor {

	private static final ThreadLocal<Boolean> nativePagedQueryFlag = new ThreadLocal<>();

	private static final ThreadLocal<Boolean> nativePagedCountQueryFlag = new ThreadLocal<>();

	private static final ThreadLocal<Dialect> currentDialect = new ThreadLocal<>();

	public static void startNativePagedQuery(Dialect dialect) {
		nativePagedQueryFlag.set(true);
		currentDialect.set(dialect);
	}

	public static void startNativePagedCountQuery() {
		nativePagedCountQueryFlag.set(true);
	}

	public static void endNativePagedQuery() {
		nativePagedQueryFlag.remove();
		currentDialect.remove();
	}

	public static void endNativePagedCountQuery() {
		nativePagedCountQueryFlag.remove();
	}

	public static boolean isNativePagedCountQuery() {
		return nativePagedCountQueryFlag.get() != null;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		if (isNativePagedQuery()) {
			StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
			@SuppressWarnings("unchecked")
			Map<String, Object> parameter = (Map<String, Object>) statementHandler.getBoundSql().getParameterObject();
			String rawSql = statementHandler.getBoundSql().getSql();
			String sql = nativePagedQueryFlag.get() != null ? processPagedSql(rawSql, parameter)
					: processPagedCountSql(rawSql);
			org.springframework.data.util.ReflectionUtils.setField(ReflectionUtils.findField(BoundSql.class, "sql"),
					statementHandler.getBoundSql(), sql);
			return invocation.proceed();
		}
		else {
			return invocation.proceed();

		}
	}

	private String processPagedSql(String sql, Map<String, Object> parameter) {
		return currentDialect.get().getLimitHandler().processSql(sql, (Integer) parameter.get("pageSize"),
				(Long) parameter.get("offset"), (Long) parameter.get("offsetEnd"));
	}

	private String processPagedCountSql(String sql) {
		return QueryUtils.createCountQueryFor(sql);
	}

	private boolean isNativePagedQuery() {
		return nativePagedQueryFlag.get() != null || nativePagedCountQueryFlag.get() != null;
	}

	interface SqlProcessor {

		String process(Map<String, Object> pa, String sql);
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}
}
