package de.borisskert.boxfs.macos;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

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
        boolean create = options.contains(StandardOpenOption.CREATE);
        boolean createNew = options.contains(StandardOpenOption.CREATE_NEW);
        boolean write = options.contains(StandardOpenOption.WRITE) || options.contains(StandardOpenOption.APPEND);

        if (createNew || (create && Files.notExists(path))) {
            checkAccess(path.getParent(), AccessMode.WRITE);
            fileTree.createFile(path);
        } else if (!fileTree.exists(path)) {
            throw new NoSuchFileException(path.toString());
        } else if (write) {
            checkAccess(path, AccessMode.WRITE);
        }

        return new BoxFsByteChannel(path, fileTree);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        return new BoxFsDirectoryStream(dir, fileTree);
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        checkAccess(dir.getParent(), AccessMode.WRITE);
        fileTree.createDirectory(dir);
    }

    @Override
    public void delete(Path path) throws IOException {
        fileTree.delete(path);
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        if (source.equals(target)) {
            return;
        }

        boolean replaceExisting = Arrays.asList(options).contains(StandardCopyOption.REPLACE_EXISTING);

        if (fileTree.exists(target)) {
            if (replaceExisting) {
                fileTree.delete(target);
            }
        }

        Optional<BoxFsNode> node = fileTree.readNode(source);
        if (!node.isPresent()) {
            throw new NoSuchFileException(source.toString());
        }

        if (node.get().attributes().isDirectory()) {
            fileTree.createDirectory(target);
        } else {
            copyFile(node.get(), target);
        }
    }

    private void copyFile(BoxFsNode boxFsNode, Path target) throws IOException {
        byte[] content = boxFsNode.content();
        fileTree.createFile(target);
        fileTree.writeContent(target, ByteBuffer.wrap(content));
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        if (source.equals(target)) {
            return;
        }

        Path targetParent = target.getParent();
        if (targetParent != null && !fileTree.exists(targetParent)) {
            throw new NoSuchFileException(targetParent.toString());
        }

        moveRecursively(source, target, options);
    }

    private void moveRecursively(Path source, Path target, CopyOption... options) throws IOException {
        Optional<BoxFsNode> node = fileTree.readNode(source);
        if (!node.isPresent()) {
            throw new NoSuchFileException(source.toString());
        }

        boolean replaceExisting = Arrays.asList(options).contains(StandardCopyOption.REPLACE_EXISTING);

        if (node.get().attributes().isDirectory()) {
            if (fileTree.exists(target)) {
                if (replaceExisting) {
                    if (fileTree.isDirectory(target) && hasChildren(target)) {
                        throw new DirectoryNotEmptyException(target.toString());
                    }

                    deleteRecursively(target);
                } else {
                    throw new FileAlreadyExistsException(target.toString());
                }
            }

            fileTree.createDirectory(target);
            try (DirectoryStream<Path> stream = newDirectoryStream(source, entry -> true)) {
                for (Path entry : stream) {
                    Path targetEntry = target.resolve(entry.getFileName().toString());
                    moveRecursively(entry, targetEntry, options);
                }
            }
        } else {
            copy(source, target, options);
        }

        delete(source);
    }

    private void deleteRecursively(Path path) throws IOException {
        if (fileTree.isDirectory(path)) {
            try (DirectoryStream<Path> stream = newDirectoryStream(path, entry -> true)) {
                for (Path entry : stream) {
                    deleteRecursively(entry);
                }
            }
        }
        delete(path);
    }

    private boolean hasChildren(Path path) throws IOException {
        try (DirectoryStream<Path> stream = newDirectoryStream(path, entry -> true)) {
            return stream.iterator().hasNext();
        }
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        if (path.equals(path2)) {
            return true;
        }

        if (!fileTree.exists(path)) {
            throw new NoSuchFileException(path.toString());
        }

        if (!fileTree.exists(path2)) {
            throw new NoSuchFileException(path2.toString());
        }

        return false;
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
        if (!fileTree.exists(path)) {
            throw new NoSuchFileException(path.toString());
        }

        BoxFsNode boxFsNode = fileTree.readNode(path)
                .orElseThrow(() -> new UnsupportedOperationException("Not yet implemented"));

        BoxFsFileAttributeView view = boxFsNode.fileAttributeView();

        if (!isAllowed(view.readAttributes().permissions(), modes)) {
            throw new AccessDeniedException(path.toString());
        }
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        BoxFsNode entry = fileTree.readNode(path)
                .orElseThrow(() -> new UnsupportedOperationException("Not yet implemented"));
        return entry.fileAttributeView();
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if (fileTree.exists(path)) {
            return fileTree.readNode(path).map(BoxFsNode::attributes)
                    .map(a -> (A) a)
                    .orElseThrow(() -> new UnsupportedOperationException("Not yet implemented"));
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
        Set<OpenOption> set = new HashSet<>(options.length);
        Collections.addAll(set, options);

        if (set.isEmpty()) {
            set.add(StandardOpenOption.CREATE);
            set.add(StandardOpenOption.TRUNCATE_EXISTING);
            set.add(StandardOpenOption.WRITE);
        }

        return Channels.newOutputStream(newByteChannel(path, set));
    }
}
