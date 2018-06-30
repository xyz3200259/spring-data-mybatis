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

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mybatis.config.sample.TestConfig;
import org.springframework.data.mybatis.domain.sample.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author 7cat
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfig.class)
@Transactional
public class UserRepositoryTest {

	@Autowired
	private UserRepository repository;
	
	@Test
	public void testSave() {
		User user = new User();
		repository.save(user);
	}
	

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findUseMapper(java.lang.String)}.
	 */
	@Test
	public void testFindUseMapper() {
		repository.findUseMapper("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameAndFirstName(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameAndFirstName() {
		repository.findByLastNameAndFirstName("lastname", "firstname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameOrFirstName(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameOrFirstName() {
		repository.findByLastNameOrFirstName("lastname", "firstname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByDateOfBirthAfter(java.util.Date)}.
	 */
	@Test
	public void testFindByDateOfBirthAfter() {
		repository.findByDateOfBirthAfter(new Date());
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByDateOfBirthIsAfter(java.util.Date)}.
	 */
	@Test
	public void testFindByDateOfBirthIsAfter() {
		repository.findByDateOfBirthIsAfter(new Date());
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByDateOfBirthBefore(java.util.Date)}.
	 */
	@Test
	public void testFindByDateOfBirthBefore() {
		repository.findByDateOfBirthBefore(new Date());
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByDateOfBirthIsBefore(java.util.Date)}.
	 */
	@Test
	public void testFindByDateOfBirthIsBefore() {
		repository.findByDateOfBirthIsBefore(new Date());
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameContaining(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameContaining() {
		repository.findByLastNameContaining("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsContaining(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameIsContaining() {
		repository.findByLastNameIsContaining("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameContains(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameContains() {
		repository.findByLastNameContains("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByDateOfBirthBetween(java.util.Date, java.util.Date)}.
	 */
	@Test
	public void testFindByDateOfBirthBetween() {
		repository.findByDateOfBirthBetween(new Date(), new Date());
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByDateOfBirthIsBetween(java.util.Date, java.util.Date)}.
	 */
	@Test
	public void testFindByDateOfBirthIsBetween() {
		repository.findByDateOfBirthIsBetween(new Date(), new Date());
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameEndingWith(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameEndingWith() {
		repository.findByLastNameEndingWith("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsEndingWith(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameIsEndingWith() {
		repository.findByLastNameIsEndingWith("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameEndsWith(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameEndsWith() {
		repository.findByLastNameEndsWith("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#existByLastName(java.lang.String)}.
	 */
	@Test
	public void testExistsByLastName() {
		repository.existsByLastName("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#existByLastName(java.lang.String)}.
	 */
	@Test
	public void testCountByLastName() {
		repository.countByLastName("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByAgeGreaterThan(int)}.
	 */
	@Test
	public void testFindByAgeGreaterThan() {
		repository.findByAgeGreaterThan(22);
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByAgeIsGreaterThan(int)}.
	 */
	@Test
	public void testFindByAgeIsGreaterThan() {
		repository.findByAgeIsGreaterThan(22);
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByAgeGreaterThanEqual(int)}.
	 */
	@Test
	public void testFindByAgeGreaterThanEqual() {
		repository.findByAgeGreaterThanEqual(22);
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByAgeIsGreaterThanEqual(int)}.
	 */
	@Test
	public void testFindByAgeIsGreaterThanEqual() {
		repository.findByAgeIsGreaterThanEqual(22);
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIn(java.lang.String[])}.
	 */
	@Test
	public void testFindByLastNameIn() {
		repository.findByLastNameIn("name1", "name2");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsIn(java.lang.String[])}.
	 */
	@Test
	public void testFindByLastNameIsIn() {
		repository.findByLastNameIsIn("name1", "name2");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIs(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameIs() {
		repository.findByLastNameIs("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameEquals(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameEquals() {
		repository.findByLastNameEquals("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastName(java.lang.String)}.
	 */
	@Test
	public void testFindByLastName() {
		repository.findByLastName("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsNotNull()}.
	 */
	@Test
	public void testFindByLastNameIsNotNull() {
		repository.findByLastNameIsNotNull();
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameNotNull()}.
	 */
	@Test
	public void testFindByLastNameNotNull() {
		repository.findByLastNameNotNull();
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByAgeLessThan(int)}.
	 */
	@Test
	public void testFindByAgeLessThan() {
		repository.findByAgeLessThan(22);
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByAgeIsLessThan(int)}.
	 */
	@Test
	public void testFindByAgeIsLessThan() {
		repository.findByAgeIsLessThan(22);
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByAgeLessThanEqual(int)}.
	 */
	@Test
	public void testFindByAgeLessThanEqual() {
		repository.findByAgeLessThanEqual(22);
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByAgeIsLessThanEqual(int)}.
	 */
	@Test
	public void testFindByAgeIsLessThanEqual() {
		repository.findByAgeIsLessThanEqual(22);
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameLike()}.
	 */
	@Test
	public void testFindByLastNameLike() {
		repository.findByLastNameLike("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsLike()}.
	 */
	@Test
	public void testFindByLastNameIsLike() {
		repository.findByLastNameIsLike("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameNot(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameNot() {
		repository.findByLastNameNot("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsNot(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameIsNot() {
		repository.findByLastNameIsNot("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameNotIn(java.lang.String[])}.
	 */
	@Test
	public void testFindByLastNameNotIn() {
		repository.findByLastNameNotIn("name1", "name2");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsNotIn(java.lang.String[])}.
	 */
	@Test
	public void testFindByLastNameIsNotIn() {
		repository.findByLastNameIsNotIn("name1", "name2");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameNotLike()}.
	 */
	@Test
	public void testFindByLastNameNotLike() {
		repository.findByLastNameNotLike("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsNotLike()}.
	 */
	@Test
	public void testFindByLastNameIsNotLike() {
		repository.findByLastNameIsNotLike("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameStartingWith(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameStartingWith() {
		repository.findByLastNameStartingWith("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameIsStartingWith(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameIsStartingWith() {
		repository.findByLastNameIsStartingWith("lastname");
	}

	/**
	 * Test method for {@link org.springframework.data.mybatis.repository.sample.UserRepository#findByLastNameStartsWith(java.lang.String)}.
	 */
	@Test
	public void testFindByLastNameStartsWith() {
		repository.findByLastNameStartsWith("lastname");
	}

	@Test
	public void testFindByLastNameIsEmpty() {
		repository.findByLastNameIsEmpty();
	}
	
	@Test
	public void testFindByLastNameEmpty() {
		repository.findByLastNameEmpty();
	}
	
	@Test
	public void testFindByLastNameIsNotEmpty() {
		repository.findByLastNameIsNotEmpty();
	}
	
	@Test
	public void testFindByLastNameNotEmpty() {
		repository.findByLastNameNotEmpty();
	}
}
