package de.borisskert.boxfs.tree;

import de.borisskert.boxfs.attributes.BoxFsDirectoryAttributes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class BoxDirectory implements BoxNode {

    private final Map<String, BoxNode> children = new ConcurrentHashMap<>();

    private final String separator;
    private final BasicFileAttributes attributes;

    BoxDirectory(String separator) {
        this.separator = separator;
        this.attributes = new BoxFsDirectoryAttributes();
    }

    BoxDirectory(BoxDirectory parent) {
        this.separator = parent.separator;
        this.attributes = new BoxFsDirectoryAttributes();
    }

    @Override
    public void createDirectory(Path path) {
        children.putIfAbsent(
                path.getName(0).toString(),
                new BoxDirectory(this)
        );

        if (path.getNameCount() > 1) {
            children.get(
                            path.getName(0).toString()
                    )
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

        String firstName = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            children.putIfAbsent(
                    firstName,
                    new BoxFile()
            );
        } else {
            children.putIfAbsent(
                    firstName,
                    new BoxDirectory(this)
            );

            children.get(firstName)
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

        String firstName = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            return children.containsKey(firstName);
        }

        return children.containsKey(firstName)
                && children.get(firstName)
                .exists(path.subpath(1, path.getNameCount()));
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isDirectory(Path path) {
        return getChild(path).isDirectory();
    }

    @Override
    public boolean isFile() {
        return false;
    }

    @Override
    public boolean isFile(Path path) {
        return getChild(path).isFile();
    }

    @Override
    public BoxNode getChild(Path path) {
        if (path.getNameCount() < 1) {
            throw new IllegalArgumentException("Path must not be empty");
        }

        String firstName = path.getName(0).toString();

        if (path.getNameCount() == 1) {
            return children.get(firstName);
        }

        return children.get(
                firstName
        ).getChild(
                path.subpath(1, path.getNameCount())
        );
    }

    @Override
    public void writeContent(Path path, ByteBuffer buffer) {
        throw new UnsupportedOperationException("Cannot write content to a directory");
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
}
