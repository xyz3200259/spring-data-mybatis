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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mybatis.config.sample.TestConfig;
import org.springframework.data.mybatis.domain.sample.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * Test case for testing 
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
		assertNotNull(user.getCreatedAt());
		assertNotNull(user.getCreator());
		assertNull(user.getUpdatedAt());
		assertNull(user.getUpdator());
		repository.save(user);
		assertNotNull(user.getUpdatedAt());
		assertNotNull(user.getUpdator());
	}
	
	
	@Test
	public void testDeleteByExample() {
		User user = new User();
		repository.save(user);
		repository.delete(Example.of(user));
		assertFalse(repository.findById(user.getId()).isPresent());
	}

	@Test
	public void testFindUseMapper() {
		User user = new User();
		user.setLastName("lastname");
		repository.save(user);
		User user1 = new User();
		user1.setLastName("lastname");
		repository.save(user1);
		repository.findUseMapper("lastname", PageRequest.of(0, 1));
		repository.findUseMapper("lastname");
	}

	@Test
	public void testFindByLastNameAndFirstName() {
		repository.findByLastNameAndFirstName("lastname", null);
	}

	public void testFindByLastNameOrFirstName() {
		repository.findByLastNameOrFirstName("lastname", "firstname");
	}

	@Test
	public void testFindByDateOfBirthAfter() {
		repository.findByDateOfBirthAfter(new Date());
	}

	@Test
	public void testFindByDateOfBirthIsAfter() {
		repository.findByDateOfBirthIsAfter(new Date());
	}

	@Test
	public void testFindByDateOfBirthBefore() {
		repository.findByDateOfBirthBefore(new Date());
	}

	@Test
	public void testFindByDateOfBirthIsBefore() {
		repository.findByDateOfBirthIsBefore(new Date());
	}

	@Test
	public void testFindByLastNameContaining() {
		repository.findByLastNameContaining("lastname");
	}

	@Test
	public void testFindByLastNameIsContaining() {
		repository.findByLastNameIsContaining("lastname");
	}

	@Test
	public void testFindByLastNameContains() {
		repository.findByLastNameContains("lastname");
	}

	@Test
	public void testFindByDateOfBirthBetween() {
		repository.findByDateOfBirthBetween(new Date(), new Date());
	}

	@Test
	public void testFindByDateOfBirthIsBetween() {
		repository.findByDateOfBirthIsBetween(new Date(), new Date());
	}

	@Test
	public void testFindByLastNameEndingWith() {
		repository.findByLastNameEndingWith("lastname");
	}

	@Test
	public void testFindByLastNameIsEndingWith() {
		repository.findByLastNameIsEndingWith("lastname");
	}

	@Test
	public void testFindByLastNameEndsWith() {
		repository.findByLastNameEndsWith("lastname");
	}

	@Test
	public void testExistsByLastName() {
		repository.existsByLastName("lastname");
	}

	@Test
	public void testCountByLastName() {
		repository.countByLastName("lastname");
	}

	@Test
	public void testFindByAgeGreaterThan() {
		repository.findByAgeGreaterThan(22);
	}

	@Test
	public void testFindByAgeIsGreaterThan() {
		repository.findByAgeIsGreaterThan(22);
	}

	@Test
	public void testFindByAgeGreaterThanEqual() {
		repository.findByAgeGreaterThanEqual(22);
	}

	@Test
	public void testFindByAgeIsGreaterThanEqual() {
		repository.findByAgeIsGreaterThanEqual(22);
	}

	@Test
	public void testFindByLastNameIn() {
		repository.findByLastNameIn("name1", "name2");
	}

	@Test
	public void testFindByLastNameIsIn() {
		repository.findByLastNameIsIn("name1", "name2");
	}

	@Test
	public void testFindByLastNameIs() {
		repository.findByLastNameIs("lastname");
	}

	@Test
	public void testFindByLastNameEquals() {
		repository.findByLastNameEquals("lastname");
	}

	@Test
	public void testFindByLastName() {
		repository.findByLastName("lastname");
	}

	@Test
	public void testFindByLastNameIsNotNull() {
		repository.findByLastNameIsNotNull();
	}

	@Test
	public void testFindByLastNameNotNull() {
		repository.findByLastNameNotNull();
	}

	@Test
	public void testFindByAgeLessThan() {
		repository.findByAgeLessThan(22);
	}

	@Test
	public void testFindByAgeIsLessThan() {
		repository.findByAgeIsLessThan(22);
	}

	@Test
	public void testFindByAgeLessThanEqual() {
		repository.findByAgeLessThanEqual(22);
	}

	@Test
	public void testFindByAgeIsLessThanEqual() {
		repository.findByAgeIsLessThanEqual(22);
	}

	@Test
	public void testFindByLastNameLike() {
		repository.findByLastNameLike("lastname");
	}

	@Test
	public void testFindByLastNameIsLike() {
		repository.findByLastNameIsLike("lastname");
	}

	@Test
	public void testFindByLastNameNot() {
		repository.findByLastNameNot("lastname");
	}

	@Test
	public void testFindByLastNameIsNot() {
		repository.findByLastNameIsNot("lastname");
	}

	@Test
	public void testFindByLastNameNotIn() {
		repository.findByLastNameNotIn("name1", "name2");
	}

	@Test
	public void testFindByLastNameIsNotIn() {
		repository.findByLastNameIsNotIn("name1", "name2");
	}

	@Test
	public void testFindByLastNameNotLike() {
		repository.findByLastNameNotLike("lastname");
	}

	@Test
	public void testFindByLastNameIsNotLike() {
		repository.findByLastNameIsNotLike("lastname");
	}

	@Test
	public void testFindByLastNameStartingWith() {
		repository.findByLastNameStartingWith("lastname");
	}

	@Test
	public void testFindByLastNameIsStartingWith() {
		repository.findByLastNameIsStartingWith("lastname");
	}

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
	
	@Test
	public void testFindByAddress_streetNoStartsWith() {
		repository.findByAddress_streetNoStartsWith("streetNo");
	}
}
