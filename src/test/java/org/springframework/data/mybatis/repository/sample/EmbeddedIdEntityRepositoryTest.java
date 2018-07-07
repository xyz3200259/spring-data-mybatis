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

package org.springframework.data.mybatis.repository.sample;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mybatis.config.sample.TestConfig;
import org.springframework.data.mybatis.domain.sample.EmbeddedIdEntity;
import org.springframework.data.mybatis.domain.sample.EmbeddedKey;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * @author 7cat
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
public class EmbeddedIdEntityRepositoryTest {

	@Autowired
	private EmbeddedIdEntityRepository embeddedIdEntityRepository;

	@Test
	public void testInsertAndFindById() {
		EmbeddedIdEntity entity = build("key1", "key2", "field1");
		embeddedIdEntityRepository.insert(entity);
		assertEquals("field1", embeddedIdEntityRepository.findById(entity.getEmbeddedKey()).get().getField1());
		embeddedIdEntityRepository.deleteById(entity.getEmbeddedKey());
		assertFalse(embeddedIdEntityRepository.findById(entity.getEmbeddedKey()).isPresent());
	}

	@Test
	public void testUpdate() {
		EmbeddedIdEntity entity = build("key1", "key2", "field1");
		embeddedIdEntityRepository.insert(entity);
		entity.setField1("field2");
		embeddedIdEntityRepository.save(entity);
		assertEquals("field2", embeddedIdEntityRepository.findById(entity.getEmbeddedKey()).get().getField1());
	}

	private EmbeddedIdEntity build(String key1, String key2, String field1) {
		EmbeddedIdEntity entity = new EmbeddedIdEntity();
		entity.setField1(field1);
		EmbeddedKey key = new EmbeddedKey();
		key.setId1(key1);
		key.setId2(key2);
		entity.setEmbeddedKey(key);
		return entity;
	}
}
