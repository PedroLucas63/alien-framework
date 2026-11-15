package br.ufrn.dimap.pipeline.binders;

import br.ufrn.dimap.annotations.binding.Body;
import br.ufrn.dimap.pipeline.IParameterBinder;
import br.ufrn.dimap.pipeline.types.ProcessContext;
import br.ufrn.dimap.types.HttpHeader;
import br.ufrn.dimap.types.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Binds binary request bodies into temporary {@link File} instances and schedules
 * their deletion when request processing finishes.
 */
public class FileBodyBinder implements IParameterBinder {

    private static final long DEFAULT_MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final int BUFFER_SIZE = 8192;
    private static final Set<String> ACCEPTED_MIME_TYPES = Set.of(
            "application/octet-stream",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/webp",
            "video/webm",
            "application/pdf"
    );

    private final long maxFileSize;

    public FileBodyBinder() {
        this(DEFAULT_MAX_FILE_SIZE);
    }

    public FileBodyBinder(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    @Override
    public boolean tryBind(ProcessContext context, Parameter parameter) {
        if (BinderUtils.hasOtherContentAnnotation(parameter, Body.class)) {
            return false;
        }
        if (!parameter.getType().equals(File.class) || context.httpContext.isBodyRead) {
            return false;
        }

        HttpRequest request = context.httpContext.request;
        Optional<String> contentTypeHeader = request.getHeaders().tryGetHeader(HttpHeader.ContentType);
        if (contentTypeHeader.isEmpty() || !isAcceptedMimeType(contentTypeHeader.get())) {
            return false;
        }

        long contentLength = parseContentLength(request);
        if (contentLength <= 0 || contentLength > maxFileSize) {
            return false;
        }

        String rawFileName = request.getHeaders().tryGetHeader("X-File-Name").orElse("upload.tmp");
        String safeFileName = Paths.get(rawFileName).getFileName().toString();
        if (safeFileName.isBlank()) {
            safeFileName = "upload.tmp";
        }

        try {
            Path tempFile = Files.createTempFile("alien-upload-", "-" + safeFileName);
            copyExactBytes(request.getBodyStream(), tempFile, contentLength);
            context.httpContext.isBodyRead = true;
            BinderUtils.chainOnFinish(context, () -> deleteQuietly(tempFile));
            context.methodParameters.add(tempFile.toFile());
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write uploaded body to temporary file", e);
        }
    }

    private static boolean isAcceptedMimeType(String contentType) {
        String normalized = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return ACCEPTED_MIME_TYPES.contains(normalized);
    }

    private static long parseContentLength(HttpRequest request) {
        Optional<String> header = request.getHeaders().tryGetHeader(HttpHeader.ContentLength);
        if (header.isEmpty()) {
            return -1L;
        }
        try {
            return Long.parseLong(header.get().trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static void copyExactBytes(InputStream input, Path target, long bytesToCopy) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long remaining = bytesToCopy;
        try (OutputStream output = Files.newOutputStream(target)) {
            while (remaining > 0) {
                int toRead = (int) Math.min(buffer.length, remaining);
                int read = input.read(buffer, 0, toRead);
                if (read == -1) {
                    throw new IOException("Unexpected end of stream before Content-Length bytes were read");
                }
                output.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    private static void deleteQuietly(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException ignored) {
            // Best-effort cleanup of temporary upload file.
        }
    }
}
