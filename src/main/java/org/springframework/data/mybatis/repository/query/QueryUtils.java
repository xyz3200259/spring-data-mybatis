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

package org.springframework.data.mybatis.repository.query;

import org.springframework.data.mybatis.utils.SQLUtils;

/**
 * 
 * @author 7cat
 * @since 1.0
 */
public final class QueryUtils {

	public static String createCountQueryFor(String sql) {
		int cetIndex = SQLUtils.matchWithCET(sql);
		if (cetIndex > 0) {
			int index = SQLUtils.locateQueryInCTEStatement(sql, cetIndex);
			String cetPart = sql.substring(0, index);
			StringBuilder countSql = new StringBuilder();
			countSql.append(cetPart);
			String sqlPart = sql.toString().substring(index);
			countSql.append(" SELECT COUNT(*) FROM ( " + sqlPart + " ) as total");
			return countSql.toString();
		} else {
			return "SELECT COUNT(*) FROM ( " + sql + " ) as total";
		}
	}

}
