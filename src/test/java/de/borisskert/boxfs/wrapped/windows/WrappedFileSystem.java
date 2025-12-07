package de.borisskert.boxfs.wrapped.windows;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Set;

public class WrappedFileSystem extends FileSystem {

    final FileSystem defaultFileSystem;
    private final FileSystemProvider provider;
    private final Path realRoot;

    private static final String VIRTUAL_ROOT = "C:\\"; // visible root

    WrappedFileSystem(FileSystemProvider provider, String root) throws IOException {
        this.defaultFileSystem = FileSystems.getDefault();
        this.provider = provider;
        this.realRoot = defaultFileSystem.getPath(root).normalize();

        if (Files.exists(this.realRoot)) {
            throw new IOException("Root directory already exists: " + root);
        }

        Files.createDirectories(this.realRoot);
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        deleteRecursivelyIfExists(this.realRoot);
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singleton(wrap(realRoot));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return defaultFileSystem.getFileStores();
    }

    /**
     * Accepts virtual Windows style paths:
     *   "C:\foo\bar", "\foo\bar", "foo\bar"
     *
     * Always resolves inside realRoot so no escaping possible.
     */
    @Override
    public Path getPath(String first, String... more) {
        // combine pieces using backslash (Windows)
        StringBuilder sb = new StringBuilder(first == null ? "" : first);
        for (String m : more) {
            if (sb.length() > 0) sb.append(getSeparator());
            sb.append(m);
        }
        String raw = sb.toString();

        // normalize slashes to backslash
        raw = raw.replace('/', '\\');

        Path resolvedDelegate;
        if (isDriveLetterRoot(raw)) {
            // "C:\" or "C:\a\b"
            // strip "C:\" prefix
            String after = raw.length() == 2 ? "" : raw.substring(3); // if "C:" only, treat as root
            if (after.isEmpty()) {
                resolvedDelegate = realRoot;
            } else {
                resolvedDelegate = realRoot.resolve(after);
            }
        } else if (raw.startsWith("\\") || raw.startsWith("/")) {
            // "\foo\bar" treat as absolute-like -> relative inside realRoot
            String after = raw.substring(1);
            if (after.isEmpty()) {
                resolvedDelegate = realRoot;
            } else {
                resolvedDelegate = realRoot.resolve(after);
            }
        } else {
            // relative path
            if (raw.isEmpty()) {
                resolvedDelegate = realRoot;
            } else {
                resolvedDelegate = realRoot.resolve(raw);
            }
        }

        return wrap(resolvedDelegate.normalize());
    }

    WrappedPath wrap(Path p) {
        return new WrappedPath(this, p);
    }

    @Override
    public String getSeparator() {
        return "\\";
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return defaultFileSystem.supportedFileAttributeViews();
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return defaultFileSystem.getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return defaultFileSystem.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return defaultFileSystem.newWatchService();
    }

    public Path root() {
        return realRoot;
    }

    public static FileSystem create(String path) throws IOException {
        WrappedFileSystemProvider provider = new WrappedFileSystemProvider(path);
        return provider.createNewFileSystem();
    }

    /**
     * Build the visible virtual Windows path for a delegate path.
     * If delegate is inside realRoot -> "C:\<relative>"
     * If delegate equals realRoot -> "C:\"
     * Otherwise return delegate.toString() (relative/non-root host path).
     */
    String computeVisible(Path delegate) {
        Path real = delegate.normalize();
        if (real.startsWith(realRoot)) {
            Path relative = realRoot.relativize(real);
            if (relative.getNameCount() == 0) {
                return VIRTUAL_ROOT;
            }
            // Path#toString uses backslashes on Windows; keep that
            return VIRTUAL_ROOT + relative.toString();
        }
        // Not under our real root: show raw (should be rare)
        return real.toString();
    }

    static boolean isDriveLetterRoot(String s) {
        if (s == null || s.length() < 2) return false;
        char c0 = s.charAt(0);
        char c1 = s.charAt(1);
        return ((c0 >= 'A' && c0 <= 'Z') || (c0 >= 'a' && c0 <= 'z')) && c1 == ':';
    }

    private static void deleteRecursivelyIfExists(Path path) throws IOException {
        if (!Files.exists(path)) return;

        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteRecursivelyIfExists(entry);
                }
            }
        }

        Files.delete(path);
    }
}
