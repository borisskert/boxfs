package de.borisskert.boxfs.wrapped.windows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;
import java.util.Objects;

public class WrappedPath implements Path {

    private final WrappedFileSystem fs;
    private final Path delegate;
    private final String visiblePath;

    WrappedPath(WrappedFileSystem fs, Path delegate) {
        this.fs = Objects.requireNonNull(fs, "fs");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.visiblePath = fs.computeVisible(this.delegate);
    }

    Path unwrapped() {
        return delegate;
    }

    @Override
    public WrappedFileSystem getFileSystem() {
        return fs;
    }

    @Override
    public boolean isAbsolute() {
        return delegate.normalize().startsWith(fs.root());
    }

    @Override
    public Path getRoot() {
        return isAbsolute() ? fs.wrap(fs.root()) : null;
    }

    @Override
    public Path getFileName() {
        Path f = delegate.getFileName();
        return f == null ? null : fs.wrap(f);
    }

    @Override
    public Path getParent() {
        Path p = delegate.getParent();
        return p == null ? null : fs.wrap(p);
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
        // compare with visible representation for string form
        return visiblePath.startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        return delegate.endsWith(unwrapSameFs(other));
    }

    @Override
    public boolean endsWith(String other) {
        return visiblePath.endsWith(other);
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
        // if other is an absolute virtual path, let FS parse it
        if (other != null && other.length() >= 2 && WrappedFileSystem.isDriveLetterRoot(other)) {
            return fs.getPath(other);
        }
        if (other != null && (other.startsWith("\\") || other.startsWith("/"))) {
            return fs.getPath(other);
        }
        // otherwise treat as relative and append
        return fs.wrap(delegate.resolve(other.replace('/', '\\')));
    }

    @Override
    public Path resolveSibling(Path other) {
        return fs.wrap(delegate.resolveSibling(unwrapSameFs(other)));
    }

    @Override
    public Path resolveSibling(String other) {
        if (other != null && (other.startsWith("\\") || other.startsWith("/") || WrappedFileSystem.isDriveLetterRoot(other))) {
            return fs.getPath(other);
        }
        return fs.wrap(delegate.resolveSibling(other.replace('/', '\\')));
    }

    @Override
    public Path relativize(Path other) {
        return fs.wrap(delegate.relativize(unwrapSameFs(other)));
    }

    @Override
    public URI toUri() {
        return delegate.toUri();
    }

    @Override
    public Path toAbsolutePath() {
        // always resolve against real root
        return fs.wrap(fs.root().resolve(fs.root().relativize(delegate)).normalize());
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        Path base = fs.root();

        Path candidate = delegate.isAbsolute() ? delegate.toRealPath(options) : base.resolve(delegate).toRealPath(options);
        Path realRoot = base.toRealPath(options);

        if (!candidate.startsWith(realRoot)) {
            throw new IOException("Path escapes wrapped root: " + candidate);
        }

        return fs.wrap(candidate);
    }

    @Override
    public File toFile() {
        return delegate.toFile();
    }

    @Override
    public WatchKey register(WatchService watcher,
                             WatchEvent.Kind<?>[] events,
                             WatchEvent.Modifier... modifiers) throws IOException {
        return delegate.register(watcher, events, modifiers);
    }

    @Override
    public WatchKey register(WatchService watcher,
                             WatchEvent.Kind<?>... events) throws IOException {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WrappedPath)) return false;
        WrappedPath that = (WrappedPath) obj;
        return this.fs == that.fs && delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode() ^ System.identityHashCode(fs);
    }

    @Override
    public String toString() {
        return visiblePath;
    }

    // helpers

    private static Path unwrap(Path p) {
        if (p instanceof WrappedPath) return ((WrappedPath) p).delegate;
        return p;
    }

    private Path unwrapSameFs(Path other) {
        Objects.requireNonNull(other);
        if (!(other instanceof WrappedPath)) throw new ProviderMismatchException("Not a WrappedPath: " + other);
        WrappedPath wp = (WrappedPath) other;
        if (wp.fs != this.fs) throw new ProviderMismatchException("Different FileSystem");
        return wp.delegate;
    }
}
