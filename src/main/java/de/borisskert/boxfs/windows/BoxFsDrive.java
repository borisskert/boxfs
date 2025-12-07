package de.borisskert.boxfs.windows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class BoxFsDrive implements BoxFsNode {
    private final char driveLetter;
    private final BoxFsFileSystem fileSystem;

    private final Map<String, BoxFsNode> children = new ConcurrentHashMap<>();
    private final BoxFsDirectoryAttributes attributes = new BoxFsDirectoryAttributes();
    private final BoxFsFileAttributeView attributeView = new BoxFsFileAttributeView(
            new BoxFsDirectoryAttributes()
    );

    BoxFsDrive(BoxFsFileSystem fileSystem, char driveLetter) {
        this.fileSystem = fileSystem;
        this.driveLetter = driveLetter;
    }


    @Override
    public void createDirectory(Path path) {
        if (path.getNameCount() < 1) {
            return;
        }

        String name = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            children.putIfAbsent(
                    name,
                    new BoxFsDirectory(fileSystem, this, name)
            );
        } else {
            children.putIfAbsent(
                    name,
                    new BoxFsDirectory(fileSystem, this, name)
            );

            children.get(name)
                    .createDirectory(
                            path.subpath(1, path.getNameCount())
                    );
        }
    }

    @Override
    public void createFile(Path path) {
        if (path.getNameCount() < 1) {
            return;
        }

        String name = path.getName(0).toString();

        children.putIfAbsent(
                name,
                new BoxFsFile(fileSystem, this, name)
        );

        if (path.getNameCount() > 1) {
            children.get(name)
                    .createFile(
                            path.subpath(1, path.getNameCount())
                    );
        }
    }

    @Override
    public void delete(Path path) {
        if (path.getNameCount() < 1) {
            return;
        }

        if (path.getNameCount() == 1) {
            children.remove(path.getName(0).toString());
            return;
        }

        children.get(
                path.getName(0).toString()
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
            return children.containsKey(name);
        }

        return children.containsKey(name)
                && children.get(name)
                .exists(path.subpath(1, path.getNameCount()));
    }

    @Override
    public boolean isDirectory() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isDirectory(Path path) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isFile(Path path) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Optional<BoxFsNode> readNode(Path path) {
        if (path.getNameCount() < 1) {
            throw new IllegalArgumentException("Path must not be empty");
        }

        String name = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            return Optional.ofNullable(children.get(name));
        }

        return Optional.ofNullable(children.get(
                name
        )).flatMap(
                n -> n.readNode(path.subpath(1, path.getNameCount()))
        );
    }

    @Override
    public void writeContent(Path path, ByteBuffer buffer) {
        throw new UnsupportedOperationException("Not yet implemented");
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
        throw new UnsupportedOperationException("Not yet implemented");
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
