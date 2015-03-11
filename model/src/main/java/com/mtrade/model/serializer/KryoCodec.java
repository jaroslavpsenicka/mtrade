//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package com.mtrade.model.serializer;

import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.mtrade.model.TradeRequest;
import kafka.serializer.Decoder;

public class KryoCodec implements kafka.serializer.Encoder, Decoder {

    private Kryo kryo = new Kryo();

    @Override
    public byte[] toBytes(Object object) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        kryo.writeObject(output, object);
        output.close();
        return bos.toByteArray();
    }

    @Override
    public Object fromBytes(byte[] bytes) {
        return kryo.readObject(new Input(bytes), TradeRequest.class);
    }
}
