package de.borisskert.boxfs.wrapped;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Objects;

/**
 * WrappedPath: represents a Path inside the WrappedFileSystem.
 * <p>
 * Expects that WrappedFileSystem:
 * - has a method {@code wrap(Path)}: WrappedPath {@code wrap(Path p)}
 * - getRootDirectories() returns an Iterable with exactly one root WrappedPath,
 * as shown in the previous WrappedFileSystem implementation.
 */
public class WrappedPath implements Path {

    private final WrappedFileSystem fs;
    private final Path delegate;
    private final String path;

    public WrappedPath(WrappedFileSystem fs, Path delegate) {
        this.fs = Objects.requireNonNull(fs, "fs");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.path = makePath();
    }

    private String makePath() {
        if (delegate.isAbsolute()) {
            return fs.getSeparator() + fs.root().relativize(delegate);
        } else {
            return delegate.toString();
        }
    }

    /**
     * Returns the underlying host path (internal use).
     */
    Path unwrapped() {
        return delegate;
    }

    // -------------------- Path API --------------------

    @Override
    public WrappedFileSystem getFileSystem() {
        return fs;
    }

    @Override
    public boolean isAbsolute() {
        return delegate.isAbsolute();
    }

    @Override
    public Path getRoot() {
        Path root = delegate.getRoot();
        return root == null ? null : fs.wrap(root);
    }

    @Override
    public Path getFileName() {
        Path fn = delegate.getFileName();
        return fn == null ? null : fs.wrap(fn);
    }

    @Override
    public Path getParent() {
        Path parent = delegate.getParent();
        return parent == null ? null : fs.wrap(parent);
    }

    @Override
    public int getNameCount() {
        return delegate.getNameCount();
    }

    @Override
    public Path getName(int index) {
        return fs.wrap(delegate.getName(index));
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return fs.wrap(delegate.subpath(beginIndex, endIndex));
    }

    @Override
    public boolean startsWith(Path other) {
        return delegate.startsWith(unwrapSameFs(other));
    }

    @Override
    public boolean startsWith(String other) {
        return delegate.startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        return delegate.endsWith(unwrapSameFs(other));
    }

    @Override
    public boolean endsWith(String other) {
        return delegate.endsWith(other);
    }

    @Override
    public Path normalize() {
        return fs.wrap(delegate.normalize());
    }

    @Override
    public Path resolve(Path other) {
        return fs.wrap(delegate.resolve(unwrapSameFs(other)));
    }

    @Override
    public Path resolve(String other) {
        return fs.wrap(delegate.resolve(other));
    }

    @Override
    public Path resolveSibling(Path other) {
        return fs.wrap(delegate.resolveSibling(unwrapSameFs(other)));
    }

    @Override
    public Path resolveSibling(String other) {
        return fs.wrap(delegate.resolveSibling(other));
    }

    @Override
    public Path relativize(Path other) {
        return fs.wrap(delegate.relativize(unwrapSameFs(other)));
    }

    @Override
    public URI toUri() {
        return delegate.toUri();
    }

    /**
     * toAbsolutePath: for relative paths the path is resolved against
     * the backing root of the WrappedFileSystem
     * (not against the global host working directory).
     */
    @Override
    public Path toAbsolutePath() {
        if (delegate.isAbsolute()) {
            return fs.wrap(delegate.toAbsolutePath());
        } else {
            Path backingRoot = backingRootDelegate();
            Path resolved = backingRoot.resolve(delegate).normalize();
            return fs.wrap(resolved);
        }
    }

    /**
     * toRealPath: resolves symbolic links AND ensures the result stays
     * inside the backing root (otherwise IOException).
     */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        Path backingRoot = backingRootDelegate();

        Path candidate = (delegate.isAbsolute())
                ? delegate.toRealPath(options)
                : backingRoot.resolve(delegate).toRealPath(options);

        Path realRoot = backingRoot.toRealPath(options);
        if (!candidate.startsWith(realRoot)) {
            throw new IOException("Resolved path escapes the boxed filesystem root: " + candidate);
        }

        return fs.wrap(candidate);
    }

    @Override
    public File toFile() {
        return delegate.toFile();
    }

    @Override
    public WatchKey register(
            WatchService watcher,
            WatchEvent.Kind<?>[] events,
            WatchEvent.Modifier... modifiers
    ) throws IOException {
        return delegate.register(watcher, events, modifiers);
    }

    @Override
    public WatchKey register(
            WatchService watcher,
            WatchEvent.Kind<?>... events
    ) throws IOException {
        return delegate.register(watcher, events);
    }

    @Override
    public Iterator<Path> iterator() {
        final Iterator<Path> it = delegate.iterator();
        return new Iterator<Path>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Path next() {
                return fs.wrap(it.next());
            }
        };
    }

    @Override
    public int compareTo(Path other) {
        return delegate.compareTo(unwrapSameFs(other));
    }

    // -------------------- equals/hashCode/toString --------------------

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WrappedPath)) return false;
        WrappedPath other = (WrappedPath) obj;
        return this.fs == other.fs && this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return 31 * System.identityHashCode(fs) + delegate.hashCode();
    }

    @Override
    public String toString() {
        if (delegate.isAbsolute()) {
            return path;
        }

        Path absoluteNorm = delegate.normalize().toAbsolutePath();

        return absoluteNorm.toString();
    }


    // -------------------- Helpers --------------------

    /**
     * unwrap: if p is a WrappedPath, return the inner host path,
     * otherwise return p unchanged.
     */
    private static Path unwrap(Path p) {
        if (p instanceof WrappedPath) {
            return ((WrappedPath) p).delegate;
        }
        return p;
    }

    /**
     * unwrapSameFs: validates that "other" is a WrappedPath belonging to the same FileSystem,
     * otherwise throws ProviderMismatchException (as NIO expects).
     * <p>
     * If the other is not a WrappedPath -> ProviderMismatchException.
     */
    private Path unwrapSameFs(Path other) {
        Objects.requireNonNull(other, "other");

        if (!(other instanceof WrappedPath)) {
            throw new ProviderMismatchException("Path is not a WrappedPath: " + other);
        }

        WrappedPath wp = (WrappedPath) other;
        if (wp.fs != this.fs) {
            throw new ProviderMismatchException("Path belongs to different FileSystem");
        }

        return wp.delegate;
    }

    /**
     * Returns the backing root (host path) of the WrappedFileSystem.
     * Expects that fs.getRootDirectories() returns exactly one root which is a WrappedPath.
     */
    private Path backingRootDelegate() {
        Iterator<Path> roots = fs.getRootDirectories().iterator();

        if (!roots.hasNext()) {
            throw new IllegalStateException("WrappedFileSystem has no root");
        }

        Path rootPath = roots.next();

        return unwrap(rootPath);
    }
}
