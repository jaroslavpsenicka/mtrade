package com.mtrade.consumer;

import java.util.List;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.mtrade.dao.model.TradeRequest;
import com.mtrade.dao.model.TradeResponse;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@Controller
@RequestMapping("/market")
public class MarketController {

    @Autowired
    private TransactionIdGenerator txIdGenerator;

    @Autowired
    private RequestProcessorGateway requestProcessorGw;

    private static final Logger LOG = LoggerFactory.getLogger(MarketController.class);

    @RequestMapping(value = "/trade", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public TradeResponse trade(@Valid @RequestBody TradeRequest tradeRequest) {
        String txId = txIdGenerator.generateId();
        LOG.debug(txId + ": request received - " + tradeRequest);

        requestProcessorGw.process(tradeRequest, txId);

        return new TradeResponse(txId);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<FieldError> validationError(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors();
    }

}
