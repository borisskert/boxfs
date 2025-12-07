package de.borisskert.boxfs.wrapped.windows;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;

/**
 * DirectoryStream, der alle zurückgegebenen Path-Einträge wrappt,
 * damit sie zum WrappedFileSystem gehören.
 */
public class WrappedDirectoryStream implements DirectoryStream<Path> {

    private final WrappedFileSystem fs;
    private final DirectoryStream<Path> delegate;

    public WrappedDirectoryStream(WrappedFileSystem fs, DirectoryStream<Path> delegate) {
        this.fs = Objects.requireNonNull(fs, "fs");
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public Iterator<Path> iterator() {
        final Iterator<Path> it = delegate.iterator();

        return new Iterator<Path>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Path next() {
                Path next = it.next();
                return fs.wrap(next); // wichtig!
            }
        };
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
