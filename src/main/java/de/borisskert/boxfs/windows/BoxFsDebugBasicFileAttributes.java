package de.borisskert.boxfs.windows;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

class BoxFsDebugBasicFileAttributes implements BasicFileAttributes {

    private final BasicFileAttributes delegate;
    private final Object fileKey;

    BoxFsDebugBasicFileAttributes(BasicFileAttributes delegate, Object fileKey) {
        this.delegate = delegate;
        this.fileKey = fileKey;
    }

    @Override
    public FileTime lastModifiedTime() {
        return delegate.lastModifiedTime();
    }

    @Override
    public FileTime lastAccessTime() {
        return delegate.lastAccessTime();
    }

    @Override
    public FileTime creationTime() {
        return delegate.creationTime();
    }

    @Override
    public boolean isRegularFile() {
        return delegate.isRegularFile();
    }

    @Override
    public boolean isDirectory() {
        return delegate.isDirectory();
    }

    @Override
    public boolean isSymbolicLink() {
        return delegate.isSymbolicLink();
    }

    @Override
    public boolean isOther() {
        return delegate.isOther();
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public Object fileKey() {
        return fileKey;
    }
}
