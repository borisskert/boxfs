package de.borisskert.boxfs;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Decorator for {@link FileSystem} which closes the delegate when {@link #close()} is called.
 */
public class CloseableFileSystem extends FileSystem {
    private final FileSystem delegate;

    private final AtomicBoolean isOpen = new AtomicBoolean(true);

    private CloseableFileSystem(FileSystem delegate) {
        this.delegate = delegate;
    }

    @Override
    public FileSystemProvider provider() {
        return delegate.provider();
    }

    @Override
    public void close() throws IOException {
        isOpen.set(false);
    }

    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    @Override
    public boolean isReadOnly() {
        return delegate.isReadOnly();
    }

    @Override
    public String getSeparator() {
        return delegate.getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return delegate.getRootDirectories();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return delegate.getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return Collections.emptySet();
    }

    @Override
    public Path getPath(String first, String... more) {
        return delegate.getPath(first, more);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return delegate.getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return delegate.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return delegate.newWatchService();
    }

    public static FileSystem decorate(FileSystem delegate) {
        return new CloseableFileSystem(delegate);
    }
}
