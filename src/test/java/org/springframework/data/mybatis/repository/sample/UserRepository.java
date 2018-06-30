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

package org.springframework.data.mybatis.repository.sample;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.mybatis.annotations.Native;
import org.springframework.data.mybatis.domain.sample.User;
import org.springframework.data.mybatis.repository.support.MybatisRepository;
import org.springframework.data.repository.query.Param;

/**
 * Repository query keywords not support NEAR WITHIN REGEX
 * 
 * @author 7upcat
 */
public interface UserRepository extends MybatisRepository<User, String> {

	@Native
	List<User> findUseMapper(@Param("lastname") String lastName);

	// AND
	Stream<User> findByLastNameAndFirstName(String lastName, String firstName);

	// OR
	List<User> findByLastNameOrFirstName(String lastName, String firstName);

	// AFTER After
	List<User> findByDateOfBirthAfter(Date date);

	// After IsAfter
	List<User> findByDateOfBirthIsAfter(Date date);

	// BEFORE Before
	List<User> findByDateOfBirthBefore(Date date);

	// BEFORE IsBefore
	List<User> findByDateOfBirthIsBefore(Date date);

	//CONTAINING Containing
	List<User> findByLastNameContaining(String name);

	//CONTAINING IsContaining
	List<User> findByLastNameIsContaining(String name);

	//CONTAINING Contains
	List<User> findByLastNameContains(String name);

	//BETWEEN Between
	List<User> findByDateOfBirthBetween(Date start, Date end);

	//BETWEEN IsBetween
	List<User> findByDateOfBirthIsBetween(Date start, Date end);

	//ENDING_WITH EndingWith
	List<User> findByLastNameEndingWith(String name);

	//ENDING_WITH IsEndingWith
	List<User> findByLastNameIsEndingWith(String name);

	//ENDING_WITH EndsWith
	List<User> findByLastNameEndsWith(String name);

	//EXISTS
	boolean existsByLastName(String name);

	//COUNT
	int countByLastName(String name);

	//GREATER_THAN GreaterThan
	List<User> findByAgeGreaterThan(int age);

	//GREATER_THAN IsGreaterThan
	List<User> findByAgeIsGreaterThan(int age);

	//GREATER_THAN_EQUALS GreaterThanEqual
	List<User> findByAgeGreaterThanEqual(int age);

	//GREATER_THAN_EQUALS IsGreaterThanEqual
	List<User> findByAgeIsGreaterThanEqual(int age);

	//IN In
	List<User> findByLastNameIn(String... names);

	//IN isIn
	List<User> findByLastNameIsIn(String... names);

	//IS Is
	List<User> findByLastNameIs(String name);

	//IS Equals
	List<User> findByLastNameEquals(String name);

	//IS no keyword
	List<User> findByLastName(String name);

	//IS_EMPTY IsEmpty  ??
	List<User> findByLastNameIsEmpty();

	//IS_EMPTY Empty
	List<User> findByLastNameEmpty();

	//IS_NOT_EMPTY IsNotEmpty  ??
	List<User> findByLastNameIsNotEmpty();

	//	IS_NOT_EMPTY NotEmpty
	List<User> findByLastNameNotEmpty();

	//IS_NOT_NULL IsNotNull
	List<User> findByLastNameIsNotNull();

	//IS_NOT_NULL NotNull
	List<User> findByLastNameNotNull();

	// LESS_THAN LessThan
	List<User> findByAgeLessThan(int age);

	// LESS_THAN IsLessThan
	List<User> findByAgeIsLessThan(int age);

	// LESS_THAN_EQUAL LessThanEqual
	List<User> findByAgeLessThanEqual(int age);

	// LESS_THAN_EQUAL IsLessThanEqual
	List<User> findByAgeIsLessThanEqual(int age);

	//LIKE Like
	List<User> findByLastNameLike(String name);

	//LIKE IsLike
	List<User> findByLastNameIsLike(String name);

	//NOT Not
	List<User> findByLastNameNot(String name);

	//NOT IsNot
	List<User> findByLastNameIsNot(String name);

	//NOT_IN NotIn
	List<User> findByLastNameNotIn(String... names);

	//NOT_IN IsNotIn
	List<User> findByLastNameIsNotIn(String... name);

	//NOT_LIKE NotLike
	List<User> findByLastNameNotLike(String name);

	//NOT_LIKE IsNotLike
	List<User> findByLastNameIsNotLike(String name);

	//STARTING_WITH StartingWith
	List<User> findByLastNameStartingWith(String name);

	//STARTING_WITH IsStartingWith
	List<User> findByLastNameIsStartingWith(String name);

	//STARTING_WITH StartsWith
	List<User> findByLastNameStartsWith(String name);

}
