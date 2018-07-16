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

import org.junit.Test;

import static org.junit.Assert.*;

public class QueryUtilsTest {

	private  String withCaluseSql  = "with tmp47b6eb3d_979f_4df4_9d1e_18148667f54a (id, name, code, create_time,parent_id, remark) as ( \n"+
	"select id, name, code, create_time,parent_id,remark from org where id = ?\n "+
    "union all \n "+
	"select o.id, o.name, o.code, o.create_time, o.parent_id, o.remark  from org o  inner join  tmp47b6eb3d_979f_4df4_9d1e_18148667f54a  where o.parent_id = tmp47b6eb3d_979f_4df4_9d1e_18148667f54a.id ) \n"+
    "select id, name, code, create_time,parent_id, remark from tmp47b6eb3d_979f_4df4_9d1e_18148667f54a \"org\"";
	
	
	@Test
	public void testCreateCountQueryFor() {
		assertEquals("SELECT COUNT(*) FROM ( select name, age from user where name = ? order by name,age desc )", QueryUtils.createCountQueryFor("select name, age from user where name = ? order by name,age desc"));
	}
	
	@Test
	public void testCreateCountQueryForWithClause() {
		StringBuilder sb = new StringBuilder();
		sb.append("with tmp as ( selct * from user ) select * from tmp");
		assertEquals("with tmp as ( selct * from user ) SELECT COUNT(*) FROM (  select * from tmp )", QueryUtils.createCountQueryFor(sb.toString()));
		QueryUtils.createCountQueryFor(withCaluseSql);
	}

}

