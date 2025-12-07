package de.borisskert.boxfs.wrapped.windows;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

public class WrappedFileSystemProvider extends FileSystemProvider {

    private final WrappedFileSystem fs;
    private final FileSystemProvider delegate;

    public WrappedFileSystemProvider(String root) throws IOException {
        this.fs = new WrappedFileSystem(this, root);
        this.delegate = this.fs.defaultFileSystem.provider();
    }

    FileSystem createNewFileSystem() {
        return fs;
    }

    @Override
    public String getScheme() {
        return "boxfs";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
        throw new UnsupportedOperationException("Use BoxFs.create()");
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        return fs;
    }

    @Override
    public Path getPath(URI uri) {
        return fs.getPath(uri.getPath());
    }

    private Path unwrap(Path p) {
        return ((WrappedPath) p).unwrapped();
    }

    // -------------------------
    // Delegation
    // -------------------------

    @Override
    public SeekableByteChannel newByteChannel(Path path,
                                              Set<? extends OpenOption> options,
                                              FileAttribute<?>... attrs) throws IOException {
        return delegate.newByteChannel(unwrap(path), options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(
            Path dir, DirectoryStream.Filter<? super Path> filter
    ) throws IOException {
        WrappedPath wrappedPath = (WrappedPath) dir;

        DirectoryStream<Path> realStream =
                Files.newDirectoryStream(
                        wrappedPath.unwrapped(),
                        entry -> filter.accept(
                                fs.wrap(entry)
                        )
                );

        return new WrappedDirectoryStream(wrappedPath.getFileSystem(), realStream);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        delegate.createDirectory(unwrap(dir), attrs);
    }

    @Override
    public void delete(Path path) throws IOException {
        delegate.delete(unwrap(path));
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        delegate.copy(unwrap(source), unwrap(target), options);
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        delegate.move(unwrap(source), unwrap(target), options);
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return delegate.isSameFile(unwrap(path), unwrap(path2));
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return delegate.isHidden(unwrap(path));
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return delegate.getFileStore(unwrap(path));
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
//        path = unwrap(path);
//        for (AccessMode mode : modes) {
//            switch (mode) {
//                case READ:
//                    if (!Files.isReadable(path)) throw new AccessDeniedException(path.toString());
//                    break;
//                case WRITE:
//                    if (!Files.isWritable(path)) throw new AccessDeniedException(path.toString());
//                    break;
//                case EXECUTE:
//                    if (!Files.isExecutable(path)) throw new AccessDeniedException(path.toString());
//            }
//        }
        delegate.checkAccess(unwrap(path), modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(
            Path path,
            Class<V> type,
            LinkOption... options
    ) {
        return delegate.getFileAttributeView(unwrap(path), type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(
            Path path,
            Class<A> type,
            LinkOption... options
    ) throws IOException {
        return delegate.readAttributes(unwrap(path), type, options);
    }

    @Override
    public Map<String, Object> readAttributes(
            Path path,
            String attributes,
            LinkOption... options) throws IOException {
        return delegate.readAttributes(unwrap(path), attributes, options);
    }

    @Override
    public void setAttribute(
            Path path,
            String attribute,
            Object value,
            LinkOption... options
    ) throws IOException {
        delegate.setAttribute(unwrap(path), attribute, value, options);
    }
}
