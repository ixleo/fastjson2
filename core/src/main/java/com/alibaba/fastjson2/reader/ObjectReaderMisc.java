package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.Fnv;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class ObjectReaderMisc
        implements ObjectReader {
    static final long HASH_ADDRESS = Fnv.hashCode64("address");
    static final long HASH_PORT = Fnv.hashCode64("port");

    private final Class objectClass;

    public ObjectReaderMisc(Class objectClass) {
        this.objectClass = objectClass;
    }

    @Override
    public Object readObject(JSONReader jsonReader, long features) {
        if (jsonReader.nextIfNull()) {
            return null;
        }

        if (objectClass == InetSocketAddress.class) {
            InetAddress inetAddress = null;
            int port = 0;

            jsonReader.nextIfObjectStart();
            for (;;) {
                if (jsonReader.nextIfObjectEnd()) {
                    break;
                }
                long nameHashCode = jsonReader.readFieldNameHashCode();
                if (nameHashCode == HASH_ADDRESS) {
                    inetAddress = jsonReader.read(InetAddress.class);
                } else if (nameHashCode == HASH_PORT) {
                    port = jsonReader.readInt32();
                } else {
                    jsonReader.skipValue();
                }
            }
            jsonReader.nextIfMatch(',');
            return new InetSocketAddress(inetAddress, port);
        }

        if (objectClass == InetAddress.class) {
            String address = jsonReader.readString();
            try {
                return InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                throw new JSONException("read InetAddress error", e);
            }
        }

        if (objectClass == java.text.SimpleDateFormat.class) {
            String str = jsonReader.readString();
            return new java.text.SimpleDateFormat(str);
        }

        throw new JSONException("not support : " + objectClass.getName());
    }
}