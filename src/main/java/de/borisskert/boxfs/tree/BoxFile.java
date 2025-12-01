package de.borisskert.boxfs.tree;

import java.nio.file.Path;

class BoxFile implements BoxNode {
    @Override
    public void createDirectory(Path path) {
        throw new UnsupportedOperationException("Cannot create a directory inside a file");
    }

    @Override
    public void createFile(Path path) {
        throw new UnsupportedOperationException("Cannot create a file inside a file");
    }

    @Override
    public void delete(Path path) {
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
    public BoxNode getChild(Path path) {
        throw new UnsupportedOperationException("Cannot get a child of a file");
    }
}
