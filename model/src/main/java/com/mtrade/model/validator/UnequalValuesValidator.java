//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.model.validator;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.mtrade.model.annotation.UnequalValues;

public class UnequalValuesValidator implements ConstraintValidator<UnequalValues, Object> {

    private Pattern fieldNameMatcher;

    @Override
    public void initialize(UnequalValues constraintAnnotation) {
        this.fieldNameMatcher = Pattern.compile(constraintAnnotation.regex());
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return value == null || fieldValuesDoesNotMatch(value);
    }

    private boolean fieldValuesDoesNotMatch(Object value) {
        Set<Object> values = new HashSet<>();
        for (Field field : value.getClass().getDeclaredFields()) {
            if (fieldNameMatcher.matcher(field.getName()).matches()) {
                String fieldValue = readValue(field, value);
                if (fieldValue != null) {
                    if (values.contains(fieldValue)) {
                        return false;
                    } else {
                        values.add(fieldValue);
                    }
                }
            }
        }

        return true;
    }

    private String readValue(Field field, Object value) {
        try {
            field.setAccessible(true);
            return field.get(value).toString();
        } catch (Exception ex) {
            return null;
        }
    }

}
