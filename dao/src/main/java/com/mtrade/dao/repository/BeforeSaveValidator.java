package com.mtrade.dao.repository;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.stereotype.Component;

import com.mongodb.DBObject;

/**
 * Based on http://maciejwalkowiak.pl/blog/2012/04/20/jsr-303-validation-with-spring-data-mongodb.
 * @author jaroslav.psenicka@gmail.com
 */
@Component
public class BeforeSaveValidator extends AbstractMongoEventListener {

    @Autowired
    @Qualifier("validator")
    private Validator validator;

    private static final Logger LOG = LoggerFactory.getLogger(BeforeSaveValidator.class);

    @Override
    public void onBeforeSave(Object source, DBObject dbo) {
        Set<ConstraintViolation<Object>> violations = validator.validate(source);
        if (violations.size() > 0) {
            LOG.info("Contraints violated: "  + violations);
            throw new ConstraintViolationException(violations);
        }
    }
}