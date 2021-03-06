package com.mtrade.common.validator;

import java.util.Currency;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.mtrade.common.annotation.CurrencyCode;

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
