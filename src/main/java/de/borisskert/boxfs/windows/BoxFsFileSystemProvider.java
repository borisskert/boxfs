package de.borisskert.boxfs.windows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class BoxFsFileSystemProvider extends FileSystemProvider {
    private final BoxFsNode fileTree;

    BoxFsFileSystemProvider(BoxFsNode fileTree, String separator) {
        this.fileTree = fileTree;
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
        checkAccess(path.getParent(), AccessMode.WRITE);
        fileTree.createFile(path);

        return new BoxFsByteChannel(path, fileTree);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return new BoxFsDirectoryStream(dir, fileTree);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        checkAccess(dir.getParent(), AccessMode.WRITE); // TODO test
        fileTree.createDirectory(dir);
    }

    @Override
    public void delete(Path path) throws IOException {
        checkAccess(path, AccessMode.WRITE);
        fileTree.delete(path);
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
        if (Files.notExists(path)) {
            throw new FileNotFoundException(path.toString());
        }

        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        if (!fileTree.exists(path)) {
            throw new NoSuchFileException(path.toString());
        }

        Optional<BoxFsNode> boxFsNode = fileTree.readNode(path);
        if (!boxFsNode.isPresent()) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        BoxFsFileAttributeView view = boxFsNode.get().fileAttributeView();
        BoxFsAttributes attributes = (BoxFsAttributes) view.readAttributes();

        attributes.checkAccess(modes);
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        if (PosixFileAttributeView.class.equals(type)) {
            throw new UnsupportedOperationException("PosixFileAttributeView not supported");
        }

        return fileTree.readNode(path)
                .map(BoxFsNode::fileAttributeView)
                .map(v -> (V) v)
                .orElseThrow(() -> new UnsupportedOperationException("Not yet implemented"));
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if (PosixFileAttributes.class.equals(type)) {
            throw new UnsupportedOperationException("PosixFileAttributes not supported");
        }

        return fileTree.readNode(path)
                .map(BoxFsNode::attributes)
                .map(a -> (A) a)
                .orElseThrow(() -> new NoSuchFileException(path.toString()));
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        BoxFsBasicAttributesMap attributesAsMap = fileTree.readNode(path)
                .map(BoxFsNode::attributes)
                .map(a -> (BoxFsAttributes) a)
                .map(BoxFsAttributes::toMap)
                .orElseThrow(() -> new NoSuchFileException(path.toString()));

        return attributesAsMap.readAttributes(attributes);
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        BoxFsBasicAttributesMap attributesAsMap = fileTree.readNode(path)
                .map(BoxFsNode::attributes)
                .map(a -> (BoxFsAttributes) a)
                .map(BoxFsAttributes::toMap)
                .orElseThrow(() -> new NoSuchFileException(path.toString()));

        attributesAsMap.put(attribute, value);
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
