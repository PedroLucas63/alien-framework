package br.ufrn.dimap.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpStreamReader {
    private final InputStream inputReader;
    private final byte[] buffer;
    private int position = 0;
    private int limit = 0;

    public HttpStreamReader(InputStream inputReader) {
        this(inputReader, 8192);
    }

    public HttpStreamReader(InputStream inputReader, int bufferSize) {
        this.inputReader = inputReader;
        this.buffer = new byte[bufferSize];
    }

    public String readLine() throws IOException {
        ByteArrayOutputStream overflow = null;

        while (true) {
            if (position >= limit) {
                position = 0;
                limit = inputReader.read(buffer, 0, buffer.length);
                if (limit == -1) {
                    limit = 0;
                    if (overflow == null || overflow.size() == 0) {
                        return null; // EOF
                    }
                    return stripTrailingCr(overflow.toByteArray());
                }
            }

            for (int i = position; i < limit; i++) {
                if (buffer[i] == '\n') {
                    int len = i - position;
                    String line;
                    if (overflow == null) {
                        if (len > 0 && buffer[i - 1] == '\r') {
                            len--;
                        }
                        line = new String(buffer, position, len, StandardCharsets.US_ASCII);
                    } else {
                        overflow.write(buffer, position, len);
                        line = stripTrailingCr(overflow.toByteArray());
                    }
                    position = i + 1;
                    return line;
                }
            }

            if (overflow == null) {
                overflow = new ByteArrayOutputStream();
            }
            overflow.write(buffer, position, limit - position);
            position = limit;
        }
    }

    public int read(byte[] dest, int off, int len) throws IOException {
        if (position < limit) {
            int available = limit - position;
            int toCopy = Math.min(len, available);
            System.arraycopy(buffer, position, dest, off, toCopy);
            position += toCopy;
            return toCopy;
        }
        return inputReader.read(dest, off, len);
    }

    private static String stripTrailingCr(byte[] bytes) {
        int len = bytes.length;
        if (len > 0 && bytes[len - 1] == '\r') {
            len--;
        }
        return new String(bytes, 0, len, StandardCharsets.US_ASCII);
    }
}