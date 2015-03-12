//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import com.mtrade.common.validator.CurrencyCodeValidator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = CurrencyCodeValidator.class)
@Documented
public @interface CurrencyCode {

    String message() default "invalid currency code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


}
