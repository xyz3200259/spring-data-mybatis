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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.GenerationType;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mybatis.id.IdentityGenerator;
import org.springframework.data.mybatis.id.IdentityGeneratorFactory;

/**
 * @author 7cat
 * @since 1.0
 */
public class DefaultIdentityGeneratorFactory<ID extends Serializable, T, P extends PersistentProperty<P>>
		implements IdentityGeneratorFactory<ID, T, P> {

	public DefaultIdentityGeneratorFactory(SqlSessionFactory sqlSessionFactory) {
		autoGenerationTypeGeneratorMapping.put("uuid", new UUIDGenerator());
	}

	private Map<String, IdentityGenerator<? extends Serializable>> autoGenerationTypeGeneratorMapping = new HashMap<>();

	@Autowired(required = false)
	private CustomIdentityGeneratorFactory<ID, T, P> customIdentityGeneratorFactory;

	@SuppressWarnings("unchecked")
	@Override
	public IdentityGenerator<ID> resolve(GenerationType type, String generator, PersistentEntity<T, P> pe) {
		IdentityGenerator<ID> identityGenerator = null;
		if (customIdentityGeneratorFactory != null) {
			identityGenerator = customIdentityGeneratorFactory.resolve(type, generator, pe);
		}
		if (identityGenerator != null) {
			return identityGenerator;
		}
		if (type == GenerationType.AUTO) {
			identityGenerator = (IdentityGenerator<ID>) autoGenerationTypeGeneratorMapping.get(generator);
		}
		return identityGenerator;
	}

	public void setCustomIdentityGeneratorFactory(
			CustomIdentityGeneratorFactory<ID, T, P> customIdentityGeneratorFactory) {
		this.customIdentityGeneratorFactory = customIdentityGeneratorFactory;
	}
}
