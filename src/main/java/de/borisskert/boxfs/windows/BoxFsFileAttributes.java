package de.borisskert.boxfs.windows;

import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

class BoxFsFileAttributes extends BoxFsAttributes {
    private final Supplier<Long> sizeSupplier;

    public BoxFsFileAttributes(Supplier<Long> sizeSupplier) {
        super(BoxFsBasicAttributesMap.empty());
        this.sizeSupplier = sizeSupplier;
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
        return true;
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
        return sizeSupplier.get();
    }

    @Override
    public Object fileKey() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
