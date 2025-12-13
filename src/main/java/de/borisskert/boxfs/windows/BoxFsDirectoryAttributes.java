package de.borisskert.boxfs.windows;

import java.nio.file.attribute.FileTime;

class BoxFsDirectoryAttributes extends BoxFsAttributes {
    public BoxFsDirectoryAttributes() {
        super(BoxFsBasicAttributesMap.empty().put("dos:readonly", Boolean.FALSE));
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
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
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
