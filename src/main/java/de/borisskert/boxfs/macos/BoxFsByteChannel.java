package de.borisskert.boxfs.macos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class BoxFsByteChannel implements SeekableByteChannel {
    private final AtomicBoolean isOpen = new AtomicBoolean(true);
    private final AtomicInteger position = new AtomicInteger(0);

    private final Path path;
    private final BoxFsNode tree;

    public BoxFsByteChannel(Path path, BoxFsNode tree) {
        this.path = path;
        this.tree = tree;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        ensureOpen();

        BoxFsNode file = tree.readNode(path);
        byte[] content = file.content();
        int size = (int) file.attributes().size();

        if (position.get() >= size) {
            return -1; // EOF
        }

        int bytesAvailable = size - position.get();
        int bytesToRead = Math.min(bytesAvailable, dst.remaining());

        dst.put(content, position.get(), bytesToRead);

        position.set(position.get() + bytesToRead);

        return bytesToRead;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int bytes = src.remaining();  // entscheidend!
        tree.readNode(path).writeContent(path, src);
        return bytes;
    }

    @Override
    public long position() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public long size() throws IOException {
        return tree.readNode(path).attributes().size();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    @Override
    public void close() throws IOException {
        isOpen.set(false);
    }

    private void ensureOpen() throws ClosedChannelException {
        if (!isOpen.get()) throw new ClosedChannelException();
    }
}
