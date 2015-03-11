package com.mtrade.consumer;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.mtrade.model.TradeRequest;
import com.mtrade.model.TradeResponse;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Controller
public class MarketController {

    @Autowired
    private TransactionIdGenerator txIdGenerator;

    @Autowired
    private RequestProcessorGateway requestProcessorGw;

    private static final Logger LOG = LoggerFactory.getLogger(MarketController.class);

    @ResponseBody
    @RequestMapping(value = "/request", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TradeResponse trade(@Valid @RequestBody TradeRequest tradeRequest, HttpServletRequest httpRequest) {
        String txId = txIdGenerator.generateId();
        tradeRequest.setTransactionId(txId);
        LOG.info(txId + ": request received from " + httpRequest.getRemoteAddr());
        requestProcessorGw.process(tradeRequest, txId);
        return new TradeResponse(txId);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validationError(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        for (ObjectError objectError : ex.getBindingResult().getGlobalErrors()) {
            errors.put(objectError.getObjectName(), objectError.getDefaultMessage());
        }

        return errors;
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> generalError(Exception ex) {
        LOG.error("Error processing request", ex);
        Map<String, String> errors = new HashMap<>();
        errors.put("tradeRequest", ex.getMessage());
        return errors;
    }
}
