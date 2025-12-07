package de.borisskert.boxfs.path;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

public class DefaultTest extends PathTest {
    @Override
    FileSystem getFs() throws IOException {
//        return ContextFileSystem.decorate(
//                CloseableFileSystem.decorate(FileSystems.getDefault())
//        ).withRoot(Paths.get("/tmp/boxfs-test"));
//        return WrappedFileSystem.create(Paths.get("/tmp/boxfs-test"));
        return FileSystems.getDefault();
    }
}
