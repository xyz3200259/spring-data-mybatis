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

package org.springframework.data.mybatis.id.support;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mybatis.config.sample.TestConfig;
import org.springframework.data.mybatis.domain.sample.User;
import org.springframework.data.mybatis.repository.dialect.Dialect;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;


/**
 * 
 * @author 7cat
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
public class TableGeneratorTest <P extends PersistentProperty<P>>{

	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private Dialect dialect;
	
	@Test
	public void testGenerate() {
		TableGenerator generator = new TableGenerator(dataSource, dialect);
		PersistentProperty<?>  pp=Mockito.mock(PersistentProperty.class);
		Mockito.when(pp.getOwner()).thenAnswer((InvocationOnMock mock)->{
			PersistentEntity<?, ?> pe = Mockito.mock(PersistentEntity.class);
			Mockito.when(pe.getName()).thenAnswer((InvocationOnMock iom)->User.class.getName());
			return pe;
		});
		Mockito.when(pp.getName()).thenReturn("id");
		assertEquals(new Long(1), generator.generate(pp));
		assertEquals(new Long(2), generator.generate(pp));
		assertEquals(new Long(3), generator.generate(pp));
		assertEquals(new Long(4), generator.generate(pp));
	}
}
