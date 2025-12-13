package de.borisskert.boxfs.unix;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;

class BoxFsDirectoryStream implements DirectoryStream<Path> {

    private final Path directoryPath;
    private final BoxFsNode fileTree;

    public BoxFsDirectoryStream(Path directoryPath, BoxFsNode fileTree) {
        this.directoryPath = directoryPath;
        this.fileTree = fileTree;
    }

    @Override
    public Iterator<Path> iterator() {
        Collection<String> children = fileTree.readNode(directoryPath)
                .children();

        return children.stream()
                .map(directoryPath::resolve)
                .iterator();
    }

    @Override
    public void close() throws IOException {

    }
}
