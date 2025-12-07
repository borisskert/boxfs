package de.borisskert.boxfs.filesystem;

import de.borisskert.boxfs.wrapped.WrappedFileSystem;

import java.io.IOException;
import java.nio.file.FileSystem;

public class DefaultTest extends FileSystemTest {
    @Override
    FileSystem getFs() throws IOException {
//        return ContextFileSystem.decorate(
//                CloseableFileSystem.decorate(FileSystems.getDefault())
//        ).withRoot(Paths.get("/tmp/boxfs-test"));
//        return CloseableFileSystem.decorate(
//                WrappedFileSystem.create("/tmp/boxfs-test")
//        );
        return WrappedFileSystem.create("/tmp/boxfs-test");
    }
}
