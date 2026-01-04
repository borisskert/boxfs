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

class BoxFsDirectory implements BoxFsNode {

    private final BoxFsFileSystem fileSystem;
    private final BoxFsNode parent;
    private String name;
    private final Map<BoxFsFileName, BoxFsNode> children = new ConcurrentHashMap<>();

    private final BoxFsDirectoryAttributes attributes = new BoxFsDirectoryAttributes();
    private final BoxFsFileAttributeView attributeView = new BoxFsFileAttributeView(
            new BoxFsDirectoryAttributes()
    );

    BoxFsDirectory(BoxFsFileSystem fileSystem, BoxFsNode parent, String name) {
        this.fileSystem = fileSystem;
        this.parent = parent;
        this.name = name;
    }

    @Override
    public void createDirectory(Path path) throws IOException {
        BoxFsFileName directoryName = BoxFsFileName.of(path.getName(0).toString());

        if (path.getNameCount() == 1) {
            if (children.containsKey(directoryName)) {
                throw new FileAlreadyExistsException(path.toString());
            }

            children.put(
                    directoryName,
                    new BoxFsDirectory(fileSystem, this, directoryName.name())
            );
        } else {
            BoxFsNode nextDirectory = children.computeIfAbsent(
                    directoryName,
                    name -> new BoxFsDirectory(fileSystem, this, name.name())
            );

            nextDirectory.createDirectory(path.subpath(1, path.getNameCount()));
        }
    }

    @Override
    public void createFile(Path path) throws IOException {
        if (path.getNameCount() < 1) {
            return;
        }

        BoxFsFileName childName = BoxFsFileName.of(path.getName(0).toString());

        if (path.getNameCount() == 1) {
            BoxFsNode existing = children.get(childName);
            if (existing != null) {
                if (existing.isDirectory()) {
                    throw new AccessDeniedException(path.toString());
                }

                throw new FileAlreadyExistsException(path.toString());
            }

            children.put(
                    childName,
                    new BoxFsFile(fileSystem, this, childName.name())
            );
        } else {
            BoxFsNode nextDirectory = children.computeIfAbsent(
                    childName,
                    name -> new BoxFsDirectory(fileSystem, this, name.name())
            );

            nextDirectory.createFile(path.subpath(1, path.getNameCount()));
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

        return Optional.ofNullable(
                children.get(BoxFsFileName.of(name))
        ).flatMap(
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
        A attrs = (A) attributes;
        return attrs;
    }

    @Override
    public byte[] content() throws IOException {
        throw new UnsupportedOperationException("Cannot read content from a directory");
    }

    @Override
    public <V extends FileAttributeView> V fileAttributeView() {
        @SuppressWarnings("unchecked")
        V view = (V) this.attributeView;
        return view;
    }

    @Override
    public Collection<String> children() {
        return children.keySet().stream()
                .map(BoxFsFileName::name)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<BoxFsNode> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public void rename(String newName) {
        this.name = newName;
    }

    @Override
    public void rename(Path source, Path target) throws IOException {
        if (source.getNameCount() < 1) {
            return;
        }

        BoxFsFileName sourceName = BoxFsFileName.of(source.getName(0).toString());

        if (source.getNameCount() == 1) {
            BoxFsNode node = children.remove(sourceName);
            if (node != null) {
                String targetName = target.getFileName().toString();
                node.rename(targetName);
                children.put(BoxFsFileName.of(targetName), node);
            }
        } else {
            children.get(sourceName).rename(
                    source.subpath(1, source.getNameCount()),
                    target
            );
        }
    }

    @Override
    public BoxFsPath path() {
        if (parent == null) {
            return fileSystem.root();
        }

        return parent.path().resolve(name);
    }

    @Override
    public Iterable<Path> rootDirectories() {
        throw new UnsupportedOperationException("Not supported to get root directories from directory");
    }

    @Override
    public String toString() {
        return name;
    }
}
