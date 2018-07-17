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

package org.springframework.data.mybatis.repository.dialect;

import org.springframework.data.mybatis.repository.dialect.pagination.AbstractLimitHandler;
import org.springframework.data.mybatis.repository.dialect.pagination.LimitHandler;

/**
 * An SQL dialect for Oracle.
 * 
 * @author Jarvis Song
 */
public class OracleDialect extends Dialect {

	private static final AbstractLimitHandler LIMIT_HANDLER = new AbstractLimitHandler() {

		@Override
		public boolean supportsLimit() {
			return true;
		}

		@Override
		public boolean bindLimitParametersInReverseOrder() {
			return true;
		}

		@Override
		public String processSql(String columns, String from, String condition, String sorts) {
			return processSql("select " + columns + from + condition + sorts);
		}

		@Override
		public String processSql(String sql, int pageSize, long offset, long offsetEnd) {
			final StringBuilder pagingSelect = new StringBuilder();
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
			pagingSelect.append(sql);
			pagingSelect.append(" ) row_ where rownum  <=" + offsetEnd + ") where rownum_ > " + offset);
			return pagingSelect.toString();
		}

		private String processSql(String sql) {
			final StringBuilder pagingSelect = new StringBuilder();
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
			pagingSelect.append(sql);
			pagingSelect.append(" ) row_ where rownum <![CDATA[<=]]> #{offsetEnd}) where rownum_ <![CDATA[>]]> #{offset}");
			return pagingSelect.toString();
		};
	};

	@Override
	public LimitHandler getLimitHandler() {
		return LIMIT_HANDLER;
	}

	@Override
	public char closeQuote() {
		return '"';
	}

	@Override
	public char openQuote() {
		return '"';
	}

	@Override
	public boolean supportsDeleteAlias() {
		return false;
	}
}
