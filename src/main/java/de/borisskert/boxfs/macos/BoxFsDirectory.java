package de.borisskert.boxfs.macos;

import java.io.IOException;
import java.nio.ByteBuffer;
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
    private BoxFsDirectory parent;
    private String name;
    private final Map<BoxFsFileName, BoxFsNode> children = new ConcurrentHashMap<>();

    private final BoxFsDirectoryAttributes attributes = new BoxFsDirectoryAttributes();
    private final BoxFsFileAttributeView attributeView = new BoxFsFileAttributeView(
            new BoxFsDirectoryAttributes()
    );

    BoxFsDirectory(BoxFsFileSystem fileSystem, BoxFsDirectory parent, String name) {
        this.fileSystem = fileSystem;
        this.parent = parent;
        this.name = name;
    }

    @Override
    public void createDirectory(Path path) throws IOException {
        BoxFsFileName childName = BoxFsFileName.of(path.getName(0).toString());

        if (path.getNameCount() == 1) {
            if (children.containsKey(childName)) {
                throw new FileAlreadyExistsException(path.toString());
            }

            children.put(
                    childName,
                    new BoxFsDirectory(fileSystem, this, childName.name())
            );
        } else {
            BoxFsNode nextDirectory = children.computeIfAbsent(
                    childName,
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
            if (children.containsKey(childName)) {
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
        return readNode(path).map(BoxFsNode::isDirectory)
                .orElse(false);
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isFile(Path path) {
        return readNode(path).map(BoxFsNode::isFile)
                .orElse(false);
    }

    @Override
    public Optional<BoxFsNode> readNode(Path path) {
        if (path == null || path.getNameCount() < 1) {
            return Optional.of(this);
        }

        String name = path.getName(0).toString();

        BoxFsNode child = children.get(BoxFsFileName.of(name));
        if (child == null) {
            return Optional.empty();
        }

        if (path.getNameCount() == 1) {
            return Optional.of(child);
        }

        return child.readNode(
                path.subpath(1, path.getNameCount())
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
        if (source.getNameCount() == 1) {
            BoxFsFileName sourceFileName = BoxFsFileName.of(source.getName(0).toString());
            BoxFsFileName targetFileName = BoxFsFileName.of(target.getName(0).toString());

            BoxFsNode node = children.remove(sourceFileName);
            node.rename(targetFileName.name());

            children.put(targetFileName, node);
        } else {
            BoxFsFileName childName = BoxFsFileName.of(source.getName(0).toString());
            BoxFsNode nextDirectory = children.get(childName);

            nextDirectory.rename(
                    source.subpath(1, source.getNameCount()),
                    target.subpath(1, target.getNameCount())
            );
        }
    }

    @Override
    public void move(Path source, Path target) throws IOException {
        BoxFsNode sourceNode = readNode(source)
                .orElseThrow(() -> new java.nio.file.NoSuchFileException(source.toString()));

        Path targetParentPath = target.getParent();
        BoxFsDirectory targetParent = targetParentPath == null ?
                (BoxFsDirectory) readNode(null).get() :
                (BoxFsDirectory) readNode(targetParentPath)
                        .orElseThrow(() -> new java.nio.file.NoSuchFileException(targetParentPath.toString()));

        sourceNode.parent().ifPresent(parent -> {
            BoxFsDirectory parentDir = (BoxFsDirectory) parent;
            parentDir.children.values().remove(sourceNode);
        });

        sourceNode.rename(target.getFileName().toString());
        targetParent.children.put(BoxFsFileName.of(target.getFileName().toString()), sourceNode);

        if (sourceNode instanceof BoxFsFile) {
            BoxFsFile fileNode = (BoxFsFile) sourceNode;
            fileNode.setParent(targetParent);
        } else if (sourceNode instanceof BoxFsDirectory) {
            BoxFsDirectory dirNode = (BoxFsDirectory) sourceNode;
            dirNode.setParent(targetParent);
        }
    }

    void setParent(BoxFsDirectory parent) {
        this.parent = parent;
    }

    @Override
    public BoxFsPath path() {
        if (parent == null) {
            return fileSystem.root();
        }

        return parent.path().resolve(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
