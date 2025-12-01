package de.borisskert.boxfs.attributes;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

class NoSuchFileAttributes implements BasicFileAttributes {
    @Override
    public FileTime lastModifiedTime() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public FileTime lastAccessTime() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public FileTime creationTime() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isRegularFile() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public long size() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Object fileKey() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
