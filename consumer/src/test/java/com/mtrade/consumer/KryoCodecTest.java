package com.mtrade.consumer;

import com.mtrade.common.model.TradeRequest;
import com.mtrade.common.serializer.KryoCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public class KryoCodecTest {

    private KryoCodec codec;
    private TradeRequest request;

    @Before
    public void before() throws IOException {
        this.codec = new KryoCodec();
        request = new TradeRequest();
        request.setUserId("123");
    }

    @After
    public void after() {
    }

    @Test
    public void encodeDecode() {
        assertEquals("123", ((TradeRequest)codec.fromBytes(codec.toBytes(request))).getUserId());
    }

}

