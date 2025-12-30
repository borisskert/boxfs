package de.borisskert.boxfs.macos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Optional;

class BoxFsTree implements BoxFsNode {

    private final BoxFsFileSystem fileSystem;
    private final BoxFsDirectory rootDirectory;

    BoxFsTree(BoxFsFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.rootDirectory = new BoxFsDirectory(fileSystem, null, "/");
    }

    @Override
    public void createDirectory(Path path) throws IOException {
        rootDirectory.createDirectory(path);
    }

    @Override
    public void createFile(Path path) throws IOException {
        rootDirectory.createFile(path);
    }

    @Override
    public void delete(Path path) throws IOException {
        rootDirectory.delete(path);
    }

    @Override
    public boolean exists(Path path) {
        if (isRoot(path)) {
            return true;
        }

        return rootDirectory.exists(path);
    }

    @Override
    public boolean isDirectory() {
        return rootDirectory.isDirectory();
    }

    @Override
    public boolean isDirectory(Path path) {
        if (isRoot(path)) {
            return true;
        }

        return rootDirectory.isDirectory(path);
    }

    @Override
    public boolean isFile() {
        return rootDirectory.isFile();
    }

    @Override
    public boolean isFile(Path path) {
        if (isRoot(path)) {
            return false;
        }

        return rootDirectory.isFile(path);
    }

    @Override
    public Optional<BoxFsNode> readNode(Path path) {
        if (isRoot(path)) {
            return Optional.of(rootDirectory);
        }

        return rootDirectory.readNode(path);
    }

    @Override
    public void writeContent(Path path, ByteBuffer buffer) {
        if (isRoot(path)) {
            throw new IllegalArgumentException("Path must not be empty");
        }

        rootDirectory.writeContent(path, buffer);
    }

    @Override
    public <A extends BasicFileAttributes> A attributes() {
        return rootDirectory.attributes();
    }

    @Override
    public byte[] content() throws IOException {
        return rootDirectory.content();
    }

    @Override
    public <V extends FileAttributeView> V fileAttributeView() {
        return rootDirectory.fileAttributeView();
    }

    @Override
    public Collection<String> children() {
        return rootDirectory.children();
    }

    @Override
    public Optional<BoxFsNode> parent() {
        return Optional.empty();
    }

    @Override
    public BoxFsPath path() {
        return fileSystem.root();
    }

    private static boolean isRoot(Path path) {
        return path.isAbsolute() && path.getNameCount() < 1;
    }
}
