package com.util;

import java.io.*;

public class ByteStreams {
    private static final int BUFFER_SIZE = 8 * 1024;

    public static void copy(byte[] data, OutputStream out) throws IOException {
        try {
            out.write(data);
        } finally {
            out.close();
        }
    }

    public static long copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        long total = 0;
        int readed;
        while ((readed = in.read(buf)) != -1) {
            out.write(buf, 0, readed);
            total += readed;
        }
        out.flush();
        return total;
    }

    public static byte[] toBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    public static boolean equals(InputStream in1, InputStream in2) throws IOException {
        InputStream bin1 = new BufferedInputStream(in1);
        InputStream bin2 = new BufferedInputStream(in2);

        int ch = bin1.read();
        while (-1 != ch) {
            int ch2 = bin2.read();
            if (ch != ch2) {
                return false;
            }
            ch = bin1.read();
        }

        int ch2 = bin2.read();
        if (ch2 != -1) {
            return false;
        } else {
            return true;
        }
    }

}