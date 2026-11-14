package br.ufrn.dimap.serializers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Locale;

public class FileBodySerializer implements IHttpBodySerializer {
    @Override
    public String contentType(Object body) {
        File file = (File) body;
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        String extension = dotIndex >= 0 ? name.substring(dotIndex).toLowerCase(Locale.ROOT) : "";

        return switch (extension) {
            case ".png" -> "image/png";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".mp4" -> "video/mp4";
            case ".webm" -> "video/webm";
            case ".pdf" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    @Override
    public boolean canSerialize(Object body) {
        return body instanceof File file && file.exists() && file.isFile();
    }

    @Override
    public InputStream serialize(Object body) {
        File file = (File) body;
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException("File not found: " + file.getAbsolutePath(), e);
        }
    }
}
