package de.borisskert.boxfs.windows;

import java.io.IOException;
import java.nio.file.attribute.*;
import java.util.Set;

class BoxFsFileAttributeView implements PosixFileAttributeView {
    private final BoxFsAttributes attributes;

    public BoxFsFileAttributeView(BoxFsAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UserPrincipal getOwner() throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setOwner(UserPrincipal owner) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public PosixFileAttributes readAttributes() throws IOException {
        return attributes;
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void setPermissions(Set<PosixFilePermission> perms) throws IOException {
        attributes.setPermissions(perms);
    }

    @Override
    public void setGroup(GroupPrincipal group) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
