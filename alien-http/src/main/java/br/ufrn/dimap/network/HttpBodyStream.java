package br.ufrn.dimap.network;

import java.io.IOException;
import java.io.InputStream;

public class HttpBodyStream extends InputStream {
    private final HttpStreamReader reader;
    private final long bodyLength;
    private long bodyPosition = 0;

    public HttpBodyStream(HttpStreamReader reader, long bodyLength) {
        this.reader = reader;
        this.bodyLength = bodyLength;
    }

    @Override
    public int read() throws IOException {
        byte[] single = new byte[1];
        int read = read(single, 0, 1);
        return read == -1 ? -1 : (single[0] & 0xFF);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bodyPosition >= bodyLength) {
            return -1;
        }

        long remaining = bodyLength - bodyPosition;
        int toRead = (int) Math.min(len, remaining);

        int bytesRead = reader.read(b, off, toRead);
        if (bytesRead == -1) {
            throw new IOException("The connection was closed prematurely (incomplete body).");
        }

        bodyPosition += bytesRead;
        return bytesRead;
    }
}