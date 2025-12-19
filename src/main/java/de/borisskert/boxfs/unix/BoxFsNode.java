package de.borisskert.boxfs.unix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Optional;

interface BoxFsNode {
    static BoxFsNode newTree(BoxFsFileSystem fileSystem) {
        return new BoxFsTree(fileSystem);
    }

    void createDirectory(Path path) throws IOException;

    void createFile(Path path) throws IOException;

    void delete(Path path) throws IOException;

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

    Collection<String> children();

    Optional<BoxFsNode> parent();

    BoxFsPath path();
}
