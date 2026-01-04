package de.borisskert.boxfs.windows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Optional;

class BoxFsFile implements BoxFsNode {
    private byte[] content = new byte[0];

    private String name;
    private final BoxFsNode parent;
    private final BoxFsFileSystem fileSystem;
    private final BoxFsFileAttributes attributes;
    private final BoxFsFileAttributeView view;

    BoxFsFile(BoxFsFileSystem fileSystem, BoxFsNode parent, String name) {
        this.name = name;
        this.parent = parent;
        this.fileSystem = fileSystem;
        this.attributes = new BoxFsFileAttributes(() -> (long) content.length);
        this.view = new BoxFsFileAttributeView(this.attributes);
    }

    // -----------------------------------------------------------------------------------------------------
    // BoxNode implementations
    // -----------------------------------------------------------------------------------------------------

    @Override
    public void createDirectory(Path path) throws IOException {
        throw new UnsupportedOperationException("Cannot create a directory inside a file");
    }

    @Override
    public void createFile(Path path) throws IOException {
        throw new UnsupportedOperationException("Cannot create a file inside a file");
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("Cannot delete a file inside a file");
    }

    @Override
    public boolean exists(Path path) {
        throw new UnsupportedOperationException("Cannot check existence of a file inside a file");
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isDirectory(Path path) {
        throw new UnsupportedOperationException("Cannot check if a file inside a file is a directory");
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isFile(Path path) {
        throw new UnsupportedOperationException("Cannot check if a file inside a file is a file");
    }

    @Override
    public Optional<BoxFsNode> readNode(Path path) {
        throw new UnsupportedOperationException("Cannot get a child of a file");
    }

    @Override
    public void writeContent(Path path, ByteBuffer buffer) {
        byte[] incoming = new byte[buffer.remaining()];
        buffer.get(incoming);

        byte[] newContent = new byte[content.length + incoming.length];

        System.arraycopy(content, 0, newContent, 0, content.length);
        System.arraycopy(incoming, 0, newContent, content.length, incoming.length);

        content = newContent;
    }

    @Override
    public <A extends BasicFileAttributes> A attributes() {
        @SuppressWarnings("unchecked")
        A attrs = (A) attributes;
        return attrs;
    }

    @Override
    public byte[] content() throws IOException {
        return content;
    }

    @Override
    public <V extends FileAttributeView> V fileAttributeView() {
        @SuppressWarnings("unchecked")
        V view = (V) this.view;
        return view;
    }

    @Override
    public Collection<String> children() {
        throw new UnsupportedOperationException("Cannot get children of a file");
    }

    @Override
    public Optional<BoxFsNode> parent() {
        return Optional.of(parent);
    }

    @Override
    public void rename(String newName) {
        this.name = newName;
    }

    @Override
    public void rename(Path source, Path target) throws IOException {
        throw new UnsupportedOperationException("Cannot rename a file inside a file");
    }

    @Override
    public BoxFsPath path() {
        return new BoxFsPath(
                parent.path().getFileSystem(),
                parent.path().toString() + "/" + name
        );
    }

    @Override
    public Iterable<Path> rootDirectories() {
        throw new UnsupportedOperationException("Cannot get root directories of a file");
    }
}
