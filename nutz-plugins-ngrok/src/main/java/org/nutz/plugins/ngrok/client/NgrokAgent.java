package org.nutz.plugins.ngrok.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Encoding;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;

public class NgrokAgent {

    private static final Log log = Logs.get();

    public static void writeMsg(OutputStream out, NgrokMsg msg) throws IOException {
        synchronized (out) {
            NutMap map = new NutMap("Type", msg.remove("Type")).setv("Payload", msg);
            String cnt = Json.toJson(map, JsonFormat.tidy().setQuoteName(true));
            if (log.isDebugEnabled() && !"Ping".equals(map.get("Type")))
                log.debug("write msg = " + cnt);
            byte[] buf = cnt.getBytes(Encoding.CHARSET_UTF8);
            int len = buf.length;
            out.write(longToBytes(len, 0));
            out.write(buf);
            out.flush();
        }
    }

    @SuppressWarnings("unchecked")
    public static NgrokMsg readMsg(InputStream ins) throws IOException {
        DataInputStream dis = new DataInputStream(ins);
        byte[] lenBuf = new byte[8];
        dis.readFully(lenBuf);
        int len = (int) bytes2long(leTobe(lenBuf, 8), 0);
        byte[] buf = new byte[len];
        dis.readFully(buf);
        String cnt = new String(buf, Encoding.CHARSET_UTF8);
        NutMap map = Json.fromJson(NutMap.class, cnt);
        NgrokMsg msg = new NgrokMsg();
        msg.setv("Type", map.getString("Type"));
        Map<String, Object> payload = map.getAs("Payload", Map.class);
        if (payload == null)
            payload = new HashMap<String, Object>();
        if (log.isDebugEnabled() && !"Pong".equals(msg.get("Type")))
            log.debug("read msg = " + cnt);
        msg.putAll(payload);
        return msg;
    }

    // 下面几个方法来自 https://github.com/dosgo/ngrok-java

    // 转大端
    protected static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(0, x);
        return buffer.array();
    }

    // 转小端
    protected static byte[] longToBytes(long x, int pos) {

        byte[] bytes = longToBytes(x);
        byte[] back = new byte[8];
        // 山寨方法
        for (int i = 0; i < 8; i++) {
            back[i] = bytes[(7 - i)];
        }
        return back;
    }

    protected static byte[] leTobe(byte[] src, int len) {
        byte[] back = new byte[len];
        // 山寨方法
        for (int i = 0; i < len; i++) {
            back[i] = src[(len - 1 - i)];
        }
        return back;
    }

    protected static long bytes2long(byte[] array, int offset) {
        if (array.length < 8) {
            return 0;
        }

        return ((((long) array[offset + 0] & 0xff) << 56)
                | (((long) array[offset + 1] & 0xff) << 48)
                | (((long) array[offset + 2] & 0xff) << 40)
                | (((long) array[offset + 3] & 0xff) << 32)
                | (((long) array[offset + 4] & 0xff) << 24)
                | (((long) array[offset + 5] & 0xff) << 16)
                | (((long) array[offset + 6] & 0xff) << 8)
                | (((long) array[offset + 7] & 0xff) << 0));
    }
}
