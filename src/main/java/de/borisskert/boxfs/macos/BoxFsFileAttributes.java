package de.borisskert.boxfs.macos;

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
        super(defaultFilePermissions());
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
    public UserPrincipal owner() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public GroupPrincipal group() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Set<PosixFilePermission> defaultFilePermissions() {
        Set<PosixFilePermission> permissions = new HashSet<>();

        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_WRITE);
        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OTHERS_WRITE);

        return permissions;
    }
}
