package de.borisskert.boxfs.attributes;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.function.Supplier;

public class BoxFsFileAttributes implements BasicFileAttributes {
    private final Supplier<Long> sizeSupplier;

    public BoxFsFileAttributes(Supplier<Long> sizeSupplier) {
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
