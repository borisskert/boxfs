package de.borisskert.boxfs.windows;

import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

abstract class BoxFsAttributes implements PosixFileAttributes {
    private final AtomicReference<Set<PosixFilePermission>> permissions;

    protected BoxFsAttributes(Set<PosixFilePermission> permissions) {
        this.permissions = new AtomicReference<>(permissions);
    }

    @Override
    public Set<PosixFilePermission> permissions() {
        return permissions.get();
    }

    public void setPermissions(Set<PosixFilePermission> permissions) {
        this.permissions.set(permissions);
    }
}
