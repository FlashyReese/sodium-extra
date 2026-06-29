package me.flashyreese.mods.sodiumextra.client.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class ConfigFileIO {
    private ConfigFileIO() {
    }

    public static void writeStringAtomically(Path path, String contents) throws IOException {
        writeBytesAtomically(path, contents.getBytes(StandardCharsets.UTF_8));
    }

    public static void writeLinesAtomically(Path path, Iterable<String> lines) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append(System.lineSeparator());
        }

        writeStringAtomically(path, builder.toString());
    }

    public static void writeBytesAtomically(Path path, byte[] bytes) throws IOException {
        Path targetPath = path.toAbsolutePath();
        Path directory = targetPath.getParent();
        Path fileName = targetPath.getFileName();

        if (directory == null || fileName == null) {
            throw new IOException("Path has no parent directory: " + path);
        }

        Path tempPath = null;
        IOException failure = null;

        try {
            Files.createDirectories(directory);
            tempPath = Files.createTempFile(directory, fileName.toString(), ".tmp");

            writeBytesToTempFile(tempPath, bytes);
            Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            tempPath = null;

            forceDirectory(directory);
        } catch (IOException e) {
            failure = e;
            throw e;
        } finally {
            if (tempPath != null) {
                try {
                    Files.deleteIfExists(tempPath);
                } catch (IOException e) {
                    if (failure != null) {
                        failure.addSuppressed(e);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    public static Path moveCorruptFile(Path path) throws IOException {
        Path corruptPath = nextCorruptFilePath(path);
        Files.move(path, corruptPath);
        return corruptPath;
    }

    private static void writeBytesToTempFile(Path tempPath, byte[] bytes) throws IOException {
        try (FileChannel channel = FileChannel.open(tempPath, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }

            channel.force(true);
        }
    }

    private static Path nextCorruptFilePath(Path path) {
        Path basePath = path.resolveSibling(path.getFileName() + ".corrupt");
        if (!Files.exists(basePath)) {
            return basePath;
        }

        for (int i = 1; ; i++) {
            Path candidate = path.resolveSibling(path.getFileName() + ".corrupt." + i);
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
    }

    private static void forceDirectory(Path directory) {
        try (FileChannel channel = FileChannel.open(directory, StandardOpenOption.READ)) {
            channel.force(true);
        } catch (IOException ignored) {
        }
    }
}
