package de.borisskert.boxfs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

class BoxFsFileSystemProvider extends FileSystemProvider {
    private final BoxFsNode directories;

    BoxFsFileSystemProvider(String separator) {
        this.directories = BoxFsNode.newTree(separator);
    }

    @Override
    public String getScheme() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Path getPath(URI uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        directories.createFile(path);
        return new BoxFsByteChannel(path, directories);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        directories.createDirectory(dir);
    }

    @Override
    public void delete(Path path) throws IOException {
        directories.delete(path);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return path.equals(path2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        if (!directories.exists(path)) {
            throw new NoSuchFileException(path.toString());
        }

        BoxFsNode boxFsNode = directories.readNode(path);
        BoxFsFileAttributeView view = boxFsNode.fileAttributeView();

        if (!isAllowed(view.readAttributes().permissions(), modes)) {
            throw new AccessDeniedException(path.toString());
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        BoxFsNode entry = directories.readNode(path);
        return entry.fileAttributeView();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if (directories.exists(path)) {
            return directories.readNode(path).attributes();
        } else {
            throw new NoSuchFileException(path.toString());
        }
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new NoSuchFileException(path.toString());
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    boolean isAllowed(Set<PosixFilePermission> permissions, AccessMode... modes) {
        for (AccessMode mode : modes) {
            if (!isModeAllowed(permissions, mode)) {
                return false;
            }
        }
        return true;
    }

    private boolean isModeAllowed(Set<PosixFilePermission> perms, AccessMode mode) {
        switch (mode) {
            case READ:
                return perms.contains(PosixFilePermission.OWNER_READ)
                        || perms.contains(PosixFilePermission.GROUP_READ)
                        || perms.contains(PosixFilePermission.OTHERS_READ);

            case WRITE:
                return perms.contains(PosixFilePermission.OWNER_WRITE)
                        || perms.contains(PosixFilePermission.GROUP_WRITE)
                        || perms.contains(PosixFilePermission.OTHERS_WRITE);

            case EXECUTE:
                return perms.contains(PosixFilePermission.OWNER_EXECUTE)
                        || perms.contains(PosixFilePermission.GROUP_EXECUTE)
                        || perms.contains(PosixFilePermission.OTHERS_EXECUTE);

            default:
                return false;
        }
    }

    @Override
    public OutputStream newOutputStream(Path path, OpenOption... options) throws IOException {
        checkAccess(path, AccessMode.WRITE);
        return super.newOutputStream(path, options);
    }
}
