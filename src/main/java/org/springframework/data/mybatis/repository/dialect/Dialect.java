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

import org.springframework.data.mybatis.repository.dialect.pagination.LimitHandler;

/**
 * Database localism.
 *
 * @author Jarvis Song
 */
public interface Dialect {

	public static final String QUOTE = "`\"[";

	public static final String CLOSED_QUOTE = "`\"]";

	public default String getDatabaseId() {
		return null;
	}

	public default LimitHandler getLimitHandler() {
		return null;
	}

	public default char openQuote() {
		return '"';
	}

	public default char closeQuote() {
		return '"';
	}

	public default String quote(String name) {
		if (name == null) {
			return null;
		}

		if (name.charAt(0) == '`') {
			return openQuote() + name.substring(1, name.length() - 1)
					+ closeQuote();
		} else {
			return name;
		}
	}

	public default String wrapTableName(String tableName) {
		return tableName;
	}

	public default String wrapColumnName(String columnName) {
		return columnName;
	}

	public default boolean supportsDeleteAlias() {
		return false;
	}
}
