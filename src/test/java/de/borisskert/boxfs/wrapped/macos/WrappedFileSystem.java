package de.borisskert.boxfs.wrapped.macos;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;

public class WrappedFileSystem extends FileSystem {

    final FileSystem defaultFileSystem;
    private final FileSystemProvider provider;
    private final Path root;

    WrappedFileSystem(FileSystemProvider provider, String root) throws IOException {
        this.defaultFileSystem = FileSystems.getDefault();
        this.provider = provider;
        this.root = defaultFileSystem.getPath(root);

        if (Files.exists(this.root)) {
            throw new IOException("Root directory already exists: " + root);
        }

        Files.createDirectories(this.root);
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        deleteRecursivelyIfExists(this.root);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singleton(wrap(root));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return defaultFileSystem.getFileStores();
    }

    @Override
    public Path getPath(String first, String... more) {
        Path other = Paths.get(first, more);

        while (other.isAbsolute()) {
            first = first.substring(1);
            other = Paths.get(first, more);
        }

        return wrap(root.resolve(other));
    }

    WrappedPath wrap(Path p) {
        return new WrappedPath(this, p);
    }

    // Delegate separator, views, etc.
    @Override
    public String getSeparator() {
        return defaultFileSystem.getSeparator();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return defaultFileSystem.supportedFileAttributeViews();
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return defaultFileSystem.getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return defaultFileSystem.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return defaultFileSystem.newWatchService();
    }

    public Path root() {
        return root;
    }

    public static FileSystem create(String path) throws IOException {
        WrappedFileSystemProvider provider = new WrappedFileSystemProvider(path);
        return provider.createNewFileSystem();
    }

    private static void deleteRecursivelyIfExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursivelyIfExists(entry);
                }
            }
        }

        Files.delete(path);
    }
}
