//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.model.validator;

import java.util.Currency;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.mtrade.model.annotation.CurrencyCode;

public class CurrencyCodeValidator implements ConstraintValidator<CurrencyCode, String> {

    @Override
    public void initialize(CurrencyCode constraintAnnotation) {
        // none
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            return value == null || Currency.getInstance(value) != null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

}
