package de.borisskert.boxfs.windows;

import java.io.IOException;
import java.nio.file.attribute.*;
import java.util.Set;

class BoxFsFileAttributeView implements BasicFileAttributeView {
    private final BoxFsAttributes attributes;

    public BoxFsFileAttributeView(BoxFsAttributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public String name() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public BasicFileAttributes readAttributes() throws IOException {
        return attributes;
    }

    @Override
    public void setTimes(FileTime lastModifiedTime, FileTime lastAccessTime, FileTime createTime) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
