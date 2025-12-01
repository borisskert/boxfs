package de.borisskert.boxfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class BoxFsByteChannel implements SeekableByteChannel {
    private final AtomicBoolean isOpen = new AtomicBoolean(true);

    @Override
    public int read(ByteBuffer dst) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
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
        throw new UnsupportedOperationException("Not yet implemented");
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
}
