package com.mtrade.consumer;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mtrade.dao.model.TradeRequest;
import com.mtrade.dao.model.User;
import com.mtrade.dao.repository.UserRepository;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author jaroslav.psenicka@gmail.com
 */
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/consumer-context.xml", "classpath:/test-context.xml"})
public class MarketControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MessageReceiver messageReceiver;

    private MockMvc mockMvc;
    private User user;
    private JsonNode content;

    private static final String CONTENT = "{\"userId\": \"134256\", \"currencyFrom\": \"EUR\", " +
        "\"currencyTo\": \"GBP\", \"amountSell\": 1000, \"amountBuy\": 747.10, \"rate\": 0.7471, " +
        "\"timePlaced\" : \"24-JAN-15 10:27:44\", \"originatingCountry\" : \"FR\"}";

    @Before
    public void before() throws IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        content = new ObjectMapper().readTree(CONTENT);
    }

    @After
    public void after() {
    }

    @Test
    public void validRequest() throws Exception {
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isOk());
        Message receivedMessage = messageReceiver.getLastMessage();
        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getHeaders().get("transactionId"));
        assertEquals("134256", ((TradeRequest) receivedMessage.getPayload()).getUserId());
        assertEquals("EUR", ((TradeRequest) receivedMessage.getPayload()).getCurrencyFrom());
        assertEquals("GBP", ((TradeRequest) receivedMessage.getPayload()).getCurrencyTo());
        assertEquals("FR", ((TradeRequest) receivedMessage.getPayload()).getOriginatingCountry());
        assertEquals(new Float(747.1), ((TradeRequest) receivedMessage.getPayload()).getAmountBuy());
        assertEquals(new Float(1000.0), ((TradeRequest) receivedMessage.getPayload()).getAmountSell());
        assertEquals(new Float(0.7471), ((TradeRequest) receivedMessage.getPayload()).getRate());
    }

    @Test
    public void shortUserId() throws Exception {
        ((ObjectNode)content).put("userId", "");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void tooLongUserId() throws Exception {
        char[] chars = new char[200];
        Arrays.fill(chars, '1');
        ((ObjectNode)content).put("userId", new String(chars));
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void wrongFromCurrency() throws Exception {
        ((ObjectNode)content).put("currencyFrom", "!@#");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shortFromCurrency() throws Exception {
        ((ObjectNode)content).put("currencyFrom", "CZ");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void spaceInFromCurrency() throws Exception {
        ((ObjectNode)content).put("currencyFrom", "CZK ");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void wrongToCurrency() throws Exception {
        ((ObjectNode)content).put("currencyTo", "OMG");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void wrongCountry() throws Exception {
        ((ObjectNode)content).put("originatingCountry", "11");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void tooMuchToSell() throws Exception {
        ((ObjectNode)content).put("amountSell", "1100000");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void tooMuchToBuy() throws Exception {
        ((ObjectNode)content).put("amountBuy", "1100000");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void negativeRate() throws Exception {
        ((ObjectNode)content).put("rate", "-1");
        mockMvc.perform(post("/market/trade")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest());
    }
}

