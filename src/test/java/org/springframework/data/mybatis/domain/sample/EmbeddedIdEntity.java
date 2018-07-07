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

package org.springframework.data.mybatis.domain.sample;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 
 * @author 7cat
 * @since 1.0
 */
@Entity
@Table(name = "EMBEDDED_ID_TABLE")
public class EmbeddedIdEntity {

	@EmbeddedId
	private EmbeddedKey embeddedKey;

	private String field1;

	@Transient
	private Object transientField1;

	@org.springframework.data.annotation.Transient
	private Object transientField2;

	public EmbeddedKey getEmbeddedKey() {
		return embeddedKey;
	}

	public void setEmbeddedKey(EmbeddedKey embeddedKey) {
		this.embeddedKey = embeddedKey;
	}

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}

	public Object getTransientField1() {
		return transientField1;
	}

	public void setTransientField1(Object transientField1) {
		this.transientField1 = transientField1;
	}

	public Object getTransientField2() {
		return transientField2;
	}

	public void setTransientField2(Object transientField2) {
		this.transientField2 = transientField2;
	}

}
