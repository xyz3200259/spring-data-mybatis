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

package org.springframework.data.mybatis.annotations;

import org.springframework.data.annotation.QueryAnnotation;

import static org.springframework.data.mybatis.annotations.Native.Operation.*;

import java.lang.annotation.*;

/**
 * Annotated to named operation.
 *
 * @author Jarvis Song
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@QueryAnnotation
@Documented
public @interface Native {

    /**
     * statement name.
     *
     * @return
     */
    String value() default "";

    String namespace() default "";

    String sql() default "";

    Class<?> returnType() default Unspecified.class;

    Class<?> parameterType() default Unspecified.class;

    boolean basic() default true;

    Operation operation() default UNKNOW;

    class Unspecified {
    }

    enum Operation {
        INSERT,
        UPDATE,
        SELECT_ONE,
        SELECT_LIST,
        DELETE,
        PAGE,
        SLICE,
        STREAM,
        UNKNOW
    }
}


