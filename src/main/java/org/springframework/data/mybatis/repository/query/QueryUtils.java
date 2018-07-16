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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author 7cat
 * @since 1.0
 */
public final class QueryUtils {

	// CTE pattern support
	private static final String SPACE_NEWLINE_LINEFEED = "[\\s\\t\\n\\r]*";

	private static final Pattern WITH_CTE = Pattern.compile(
			"(^" + SPACE_NEWLINE_LINEFEED + "WITH" + SPACE_NEWLINE_LINEFEED + ")", Pattern.CASE_INSENSITIVE);

	private static final Pattern WITH_EXPRESSION_NAME = Pattern.compile(
			"(^" + SPACE_NEWLINE_LINEFEED + "[a-zA-Z0-9_]*" + SPACE_NEWLINE_LINEFEED + ")", Pattern.CASE_INSENSITIVE);

	private static final Pattern WITH_COLUMN_NAMES_START = Pattern.compile("(^" + SPACE_NEWLINE_LINEFEED + "\\()",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern WITH_COLUMN_NAMES_END = Pattern.compile("(\\))", Pattern.CASE_INSENSITIVE);

	private static final Pattern WITH_AS = Pattern.compile(
			"(^" + SPACE_NEWLINE_LINEFEED + "AS" + SPACE_NEWLINE_LINEFEED + ")", Pattern.CASE_INSENSITIVE);

	private static final Pattern WITH_COMMA = Pattern.compile("(^" + SPACE_NEWLINE_LINEFEED + ",)",
			Pattern.CASE_INSENSITIVE);

	public static String createCountQueryFor(String sql) {

		final StringBuilder sb = new StringBuilder(sql);
		if (sb.charAt(sb.length() - 1) == ';') {
			sb.setLength(sb.length() - 1);
		}
		final Matcher matcher = WITH_CTE.matcher(sb.toString());
		if (matcher.find() && matcher.groupCount() > 0) {
			// CTE SQL
			int index = locateQueryInCTEStatement(sb, matcher.end());
			String cetPart = sb.toString().substring(0, index);
			StringBuilder countSql = new StringBuilder();
			countSql.append(cetPart);
			String sqlPart = sb.toString().substring(index);
			countSql.append(" SELECT COUNT(*) FROM ( " + sqlPart + " )");
			return countSql.toString();
		}
		else {
			return "SELECT COUNT(*) FROM ( " + sql + " )";
		}
	}

	private static int locateQueryInCTEStatement(StringBuilder sql, int offset) {
		while (true) {
			Matcher matcher = WITH_EXPRESSION_NAME.matcher(sql.substring(offset));
			if (matcher.find() && matcher.groupCount() > 0) {
				offset += matcher.end();
				System.out.println(sql.substring(offset));
				matcher = WITH_COLUMN_NAMES_START.matcher(sql.substring(offset));
				if (matcher.find() && matcher.groupCount() > 0) {
					offset += matcher.end();
					matcher = WITH_COLUMN_NAMES_END.matcher(sql.substring(offset));
					if (matcher.find() && matcher.groupCount() > 0) {
						offset += matcher.end();
						offset += advanceOverCTEInnerQuery(sql, offset);
						matcher = WITH_COMMA.matcher(sql.substring(offset));
						if (matcher.find() && matcher.groupCount() > 0) {
							// another CTE fragment exists, re-start parse of CTE
							offset += matcher.end();
						}
						else {
							// last CTE fragment, we're at the start of the SQL.
							return offset;
						}
					}
					else {
						throw new IllegalArgumentException(String.format(Locale.ROOT,
								"Failed to parse CTE expression columns at offset %d, SQL [%s]", offset,
								sql.toString()));
					}
				}
				else {
					matcher = WITH_AS.matcher(sql.substring(offset));
					if (matcher.find() && matcher.groupCount() > 0) {
						offset += matcher.end();
						offset += advanceOverCTEInnerQuery(sql, offset);
						matcher = WITH_COMMA.matcher(sql.substring(offset));
						if (matcher.find() && matcher.groupCount() > 0) {
							// another CTE fragment exists, re-start parse of CTE
							offset += matcher.end();
						}
						else {
							// last CTE fragment, we're at the start of the SQL.
							return offset;
						}
					}
					else {
						throw new IllegalArgumentException(String.format(Locale.ROOT,
								"Failed to locate AS keyword in CTE query at offset %d, SQL [%s]", offset,
								sql.toString()));
					}
				}
			}
			else {
				throw new IllegalArgumentException(String.format(Locale.ROOT,
						"Failed to locate CTE expression name at offset %d, SQL [%s]", offset, sql.toString()));
			}
		}
	}

	/**
	 * Advances over the CTE inner query that is contained inside matching '(' and ')'.
	 *
	 * @param sql The sql buffer.
	 * @param offset The offset where to begin advancing the position from.
	 * @return the position immediately after the CTE inner query plus 1.
	 *
	 * @throws IllegalArgumentException if the matching parenthesis aren't detected at the end of the parse.
	 */
	private static int advanceOverCTEInnerQuery(StringBuilder sql, int offset) {
		int brackets = 0;
		int index = offset;
		boolean inString = false;
		for (; index < sql.length(); ++index) {
			if (sql.charAt(index) == '\'') {
				inString = true;
			}
			else if (sql.charAt(index) == '\'' && inString) {
				inString = false;
			}
			else if (sql.charAt(index) == '(' && !inString) {
				brackets++;
			}
			else if (sql.charAt(index) == ')' && !inString) {
				brackets--;
				if (brackets == 0) {
					break;
				}
			}
		}

		if (brackets > 0) {
			throw new IllegalArgumentException(
					"Failed to parse the CTE query inner query because closing ')' was not found.");
		}

		return index - offset + 1;
	}
}
