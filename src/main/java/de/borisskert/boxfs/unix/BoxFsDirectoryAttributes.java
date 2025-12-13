package de.borisskert.boxfs.unix;

import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

class BoxFsDirectoryAttributes extends BoxFsAttributes {
    public BoxFsDirectoryAttributes() {
        super(defaultDirectoryPermissions());
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

    @Override
    public UserPrincipal owner() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public GroupPrincipal group() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Set<PosixFilePermission> defaultDirectoryPermissions() {
        Set<PosixFilePermission> permissions = new HashSet<>();

        permissions.add(PosixFilePermission.OWNER_READ);
        permissions.add(PosixFilePermission.OWNER_WRITE);
        permissions.add(PosixFilePermission.OWNER_EXECUTE);

        permissions.add(PosixFilePermission.GROUP_READ);
        permissions.add(PosixFilePermission.GROUP_EXECUTE);

        permissions.add(PosixFilePermission.OTHERS_READ);
        permissions.add(PosixFilePermission.OTHERS_EXECUTE);

        return permissions;
    }
}
