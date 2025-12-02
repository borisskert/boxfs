package de.borisskert.boxfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;

interface BoxFsNode {
    static BoxFsNode newTree(String separator) {
        return new BoxFsDirectory(separator);
    }

    void createDirectory(Path path);

    void createFile(Path path);

    void delete(Path path);

    boolean exists(Path path);

    boolean isDirectory();

    boolean isDirectory(Path path);

    boolean isFile();

    boolean isFile(Path path);

    BoxFsNode readNode(Path path);

    void writeContent(Path path, ByteBuffer buffer);

    <A extends BasicFileAttributes> A attributes();

    byte[] content() throws IOException;

    <V extends FileAttributeView> V fileAttributeView();
}
