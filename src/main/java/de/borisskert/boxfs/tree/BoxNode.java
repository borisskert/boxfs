package de.borisskert.boxfs.tree;

import java.nio.file.Path;

public interface BoxNode {
    static BoxNode newTree(String separator) {
        return new BoxDirectory(separator);
    }

    void createDirectory(Path path);

    void createFile(Path path);

    void delete(Path path);

    boolean exists(Path path);

    boolean isDirectory();

    boolean isDirectory(Path path);

    boolean isFile();

    boolean isFile(Path path);

    BoxNode getChild(Path path);
}
