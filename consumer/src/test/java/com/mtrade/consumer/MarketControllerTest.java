package com.mtrade.consumer;

import java.io.IOException;
import java.util.Arrays;

import org.apache.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mtrade.common.model.TradeRequest;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    private WebApplicationContext webAppCtx;

    @Autowired
    private MessageReceiver messageReceiver;

    private MockMvc mockMvc;
    private JsonNode content;

    private static final String CONTENT = "{\"userId\": \"134256\", \"currencyFrom\": \"EUR\", " +
        "\"currencyTo\": \"GBP\", \"amountSell\": 1000, \"amountBuy\": 747.10, \"rate\": 0.7471, " +
        "\"timePlaced\" : \"24-JAN-15 10:27:44\", \"originatingCountry\" : \"FR\"}";

    @Before
    public void before() throws IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webAppCtx).build();
        content = new ObjectMapper().readTree(CONTENT);
    }

    @After
    public void after() {
    }

    @Test
    public void validRequest() throws Exception {
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode readTree = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertNotNull(readTree.get("txId"));
        Message receivedMessage = messageReceiver.getLastMessage();
        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getHeaders().get("transactionId"));
        assertEquals(readTree.get("txId").asText(), ((TradeRequest) receivedMessage.getPayload()).getTransactionId());
        assertEquals("134256", ((TradeRequest) receivedMessage.getPayload()).getUserId());
        assertEquals("EUR", ((TradeRequest) receivedMessage.getPayload()).getCurrencyFrom());
        assertEquals("GBP", ((TradeRequest) receivedMessage.getPayload()).getCurrencyTo());
        assertEquals("FR", ((TradeRequest) receivedMessage.getPayload()).getOriginatingCountry());
        assertEquals(new Float(747.1), ((TradeRequest) receivedMessage.getPayload()).getAmountBuy());
        assertEquals(new Float(1000.0), ((TradeRequest) receivedMessage.getPayload()).getAmountSell());
        assertEquals(new Float(0.7471), ((TradeRequest) receivedMessage.getPayload()).getRate());
        assertNotNull(((TradeRequest) receivedMessage.getPayload()).getTimePlaced());
        assertNotNull(((TradeRequest) receivedMessage.getPayload()).getTimeCreated());
    }

    @Test
    public void propertyConflict() throws Exception {
        ((ObjectNode)content).put("timeCreated", "10-10-10 10:20:30");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isOk())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("txId"));
        Message receivedMessage = messageReceiver.getLastMessage();
        assertNotNull(receivedMessage);
        long time = System.currentTimeMillis() - ((TradeRequest) receivedMessage.getPayload()).getTimeCreated().getTime();
        assertTrue(time < 10000);
    }

    @Test
    public void shortUserId() throws Exception {
        ((ObjectNode)content).put("userId", "");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("userId"));
        assertTrue(contentAsString.contains("size must be between"));
    }

    @Test
    public void tooLongUserId() throws Exception {
        char[] chars = new char[200];
        Arrays.fill(chars, '1');
        ((ObjectNode)content).put("userId", new String(chars));
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("userId"));
        assertTrue(contentAsString.contains("size must be between"));
    }

    @Test
    public void wrongFromCurrency() throws Exception {
        ((ObjectNode)content).put("currencyFrom", "!@#");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("currencyFrom"));
        assertTrue(contentAsString.contains("invalid currency code"));
    }

    @Test
    public void shortFromCurrency() throws Exception {
        ((ObjectNode)content).put("currencyFrom", "CZ");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("currencyFrom"));
        assertTrue(contentAsString.contains("invalid currency code"));
    }

    @Test
    public void spaceInFromCurrency() throws Exception {
        ((ObjectNode)content).put("currencyFrom", "CZK ");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("currencyFrom"));
        assertTrue(contentAsString.contains("invalid currency code"));
    }

    @Test
    public void wrongToCurrency() throws Exception {
        ((ObjectNode)content).put("currencyTo", "OMG");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("currencyTo"));
        assertTrue(contentAsString.contains("invalid currency code"));
    }

    @Test
    public void wrongCountry() throws Exception {
        ((ObjectNode)content).put("originatingCountry", "11");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("originatingCountry"));
        assertTrue(contentAsString.contains("country code is invalid"));
    }

    @Test
    public void tooMuchToSell() throws Exception {
        ((ObjectNode)content).put("amountSell", "1100000");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("amountSell"));
        assertTrue(contentAsString.contains("must be less"));
    }

    @Test
    public void tooMuchToBuy() throws Exception {
        ((ObjectNode)content).put("amountBuy", "1100000");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("amountBuy"));
        assertTrue(contentAsString.contains("must be less"));
    }

    @Test
    public void negativeRate() throws Exception {
        ((ObjectNode)content).put("rate", "-1");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("rate"));
        assertTrue(contentAsString.contains("must be greater than or equal to 0"));
    }

    @Test
    public void equalCurrencyCodes() throws Exception {
        ((ObjectNode)content).put("currencyTo", "EUR");
        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isBadRequest())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("tradeRequest"));
        assertTrue(contentAsString.contains("currency codes must not be equal"));
    }

    @Test
    public void generalError() throws Exception {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) webAppCtx.getAutowireCapableBeanFactory();
        beanFactory.removeBeanDefinition("transactionIdGenerator");
        beanFactory.registerBeanDefinition("transactionIdGenerator", new RootBeanDefinition(ErrorTxIdGenerator.class));

        MvcResult result = mockMvc.perform(post("/request")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .content(content.toString()))
            .andExpect(status().isInternalServerError())
            .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        assertTrue(contentAsString.contains("tradeRequest"));
        assertTrue(contentAsString.contains("expected error"));
    }
}

