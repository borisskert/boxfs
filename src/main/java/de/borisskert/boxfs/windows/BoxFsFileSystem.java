package de.borisskert.boxfs.windows;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BoxFsFileSystem extends FileSystem {
    private static final String SEPARATOR = "\\";

    private final AtomicBoolean isOpen = new AtomicBoolean(true);
    private final BoxFsNode fileTree;
    private final BoxFsFileSystemProvider provider;
    private final BoxFsPath rootPath = new BoxFsRootPath(this);

    private final java.util.Map<Path, Object> fileKeyRegistry = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.atomic.AtomicLong fileKeyGenerator = new java.util.concurrent.atomic.AtomicLong(0);

    private BoxFsFileSystem() {
        this.fileTree = BoxFsNode.newTree(this);
        this.provider = new BoxFsFileSystemProvider(fileTree, SEPARATOR);
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        isOpen.set(false);
    }

    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Iterable<Path> getRootDirectories() {
//        return Collections.singleton(rootPath);
        return fileTree.rootDirectories();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Path getPath(String first, String... more) {
        return new BoxFsPath(this, first);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    BoxFsNode getFileTree() {
        return fileTree;
    }

    BoxFsPath root() {
        return rootPath;
    }

    String separator() {
        return SEPARATOR;
    }

    public Object getOrCreateFileKey(Path path) {
        return fileKeyRegistry.computeIfAbsent(path, p -> fileKeyGenerator.incrementAndGet());
    }

    public void moveFileKey(Path source, Path target) {
        java.util.List<Path> keysToMove = fileKeyRegistry.keySet().stream()
                .filter(p -> p.startsWith(source))
                .collect(java.util.stream.Collectors.toList());

        for (Path oldPath : keysToMove) {
            Object key = fileKeyRegistry.remove(oldPath);
            if (key != null) {
                Path relative = source.relativize(oldPath);
                Path newPath = target.resolve(relative);
                fileKeyRegistry.put(newPath, key);
            }
        }
    }

    public static FileSystem create() {
        return new BoxFsFileSystem();
    }
}
