package de.borisskert.boxfs.windows;

import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

abstract class BoxFsAttributes implements BasicFileAttributes {

    protected final BoxFsBasicAttributesMap attributes;

    BoxFsAttributes(BoxFsBasicAttributesMap attributes) {
        this.attributes = attributes;
    }

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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isDirectory() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isSymbolicLink() {
        throw new UnsupportedOperationException("Not yet implemented");
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

    public BoxFsBasicAttributesMap toMap() {
        return attributes;
    }

    public boolean isReadonly() {
        return attributes.isTrue(BoxFsBasicAttributesKey.READONLY);
    }

    public void checkAccess(AccessMode[] modes) throws AccessDeniedException {
        for (AccessMode mode : modes) {
            if (mode == AccessMode.WRITE && this.isReadonly()) {
                throw new AccessDeniedException("Not allowed due to read-only attribute");
            }
        }
    }
}
