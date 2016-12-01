package com.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SizeLimitedInputStream extends FilterInputStream {
    protected InputStream in;
    protected long size;
    protected long count;

    public SizeLimitedInputStream(InputStream in, long size) {
        super(in);
        this.size = size;
    }

    public int read(byte buf[], int off, int len) throws IOException {
        if (count > size) {
            return -1;
        }
        int read = super.read(buf, off, len);
        if (read != -1) {
            count += read;
        }
        return read;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read() throws IOException {
        int result = super.read();
        if (result != -1) {
            count++;
        }
        return result;
    }

    public long skip(long n) throws IOException {
        long skip = super.skip(n);
        count += skip;
        return skip;
    }

    public boolean markSupported() {
        return false;
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
        in = null;
    }
}
