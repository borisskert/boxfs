package de.borisskert.boxfs.windows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class BoxFsDrive implements BoxFsNode {
    private final char driveLetter;
    private final BoxFsFileSystem fileSystem;

    private final Map<BoxFsFileName, BoxFsNode> children = new ConcurrentHashMap<>();
    private final BoxFsDirectoryAttributes attributes = new BoxFsDirectoryAttributes();
    private final BoxFsFileAttributeView attributeView = new BoxFsFileAttributeView(
            new BoxFsDirectoryAttributes()
    );

    BoxFsDrive(BoxFsFileSystem fileSystem, char driveLetter) {
        this.fileSystem = fileSystem;
        this.driveLetter = driveLetter;
    }


    @Override
    public void createDirectory(Path path) throws IOException {
        if (path.getNameCount() < 1) {
            return;
        }

        String name = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            if (children.containsKey(BoxFsFileName.of(name))) {
                throw new FileAlreadyExistsException(path.toString());
            }

            children.put(
                    BoxFsFileName.of(name),
                    new BoxFsDirectory(fileSystem, this, name)
            );
        } else {
            children.putIfAbsent(
                    BoxFsFileName.of(name),
                    new BoxFsDirectory(fileSystem, this, name)
            );

            children.get(BoxFsFileName.of(name))
                    .createDirectory(
                            path.subpath(1, path.getNameCount())
                    );
        }
    }

    @Override
    public void createFile(Path path) throws IOException {
        if (path.getNameCount() < 1) {
            return;
        }

        String name = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            BoxFsNode existing = children.get(BoxFsFileName.of(name));
            if (existing != null) {
                if (existing.isDirectory()) {
                    throw new AccessDeniedException(path.toString());
                }

                throw new FileAlreadyExistsException(path.toString());
            }

            children.put(
                    BoxFsFileName.of(name),
                    new BoxFsFile(fileSystem, this, name)
            );
        } else {
            children.putIfAbsent(
                    BoxFsFileName.of(name),
                    new BoxFsDirectory(fileSystem, this, name)
            );

            children.get(BoxFsFileName.of(name))
                    .createFile(
                            path.subpath(1, path.getNameCount())
                    );
        }
    }

    @Override
    public void delete(Path path) throws IOException {
        if (path.getNameCount() < 1) {
            return;
        }

        if (path.getNameCount() == 1) {
            children.remove(BoxFsFileName.of(path.getName(0).toString()));
            return;
        }

        children.get(
                BoxFsFileName.of(path.getName(0).toString())
        ).delete(
                path.subpath(1, path.getNameCount())
        );
    }

    @Override
    public boolean exists(Path path) {
        if (path.getNameCount() < 1) {
            return false;
        }

        String name = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            return children.containsKey(BoxFsFileName.of(name));
        }

        return children.containsKey(BoxFsFileName.of(name))
                && children.get(BoxFsFileName.of(name))
                .exists(path.subpath(1, path.getNameCount()));
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isDirectory(Path path) {
        return readNode(path).map(BoxFsNode::isDirectory).orElse(false);
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isFile(Path path) {
        return readNode(path).map(BoxFsNode::isFile).orElse(false);
    }

    @Override
    public Optional<BoxFsNode> readNode(Path path) {
        if (path.getNameCount() < 1) {
            throw new IllegalArgumentException("Path must not be empty");
        }

        String name = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            return Optional.ofNullable(children.get(BoxFsFileName.of(name)));
        }

        return Optional.ofNullable(children.get(
                BoxFsFileName.of(name)
        )).flatMap(
                n -> n.readNode(path.subpath(1, path.getNameCount()))
        );
    }

    @Override
    public void writeContent(Path path, ByteBuffer buffer) {
        if (path.getNameCount() < 1) {
            throw new IllegalArgumentException("Path must not be empty");
        }

        String name = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            children.get(BoxFsFileName.of(name)).writeContent(null, buffer);
        } else {
            children.get(
                    BoxFsFileName.of(name)
            ).writeContent(
                    path.subpath(1, path.getNameCount()),
                    buffer
            );
        }
    }

    @Override
    public <A extends BasicFileAttributes> A attributes() {
        @SuppressWarnings("unchecked")
        A attributes = (A) this.attributes;
        return attributes;
    }

    @Override
    public byte[] content() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public <V extends FileAttributeView> V fileAttributeView() {
        @SuppressWarnings("unchecked")
        V view = (V) this.attributeView;
        return view;
    }

    @Override
    public Collection<String> children() {
        return children.keySet()
                .stream()
                .map(BoxFsFileName::name)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<BoxFsNode> parent() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath path() {
        String path = driveLetter + ":\\";
        return new BoxFsPath(fileSystem, path);
    }

    @Override
    public Iterable<Path> rootDirectories() {
        throw new UnsupportedOperationException("Not supported to get root directories from drive");
    }
}
