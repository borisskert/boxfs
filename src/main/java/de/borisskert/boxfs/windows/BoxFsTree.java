package de.borisskert.boxfs.windows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class BoxFsTree implements BoxFsNode {
    private static final Pattern DRIVE_LETTER_PATTERN = Pattern.compile("^(?<driveletter>[A-Za-z]):\\\\.*$");

    private final BoxFsFileSystem fileSystem;
    private final Map<Character, BoxFsDrive> drives = new ConcurrentHashMap<>();

    BoxFsTree(BoxFsFileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.drives.put('C', new BoxFsDrive(fileSystem, 'C'));
    }

    @Override
    public void createDirectory(Path path) {
        if (!path.isAbsolute()) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        Optional<BoxFsNode> foundDrive = findDrive(path);

        if (path.getNameCount() < 1) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        foundDrive.ifPresent(
                drive -> drive.createDirectory(
                        path.subpath(0, path.getNameCount())
                )
        );
    }

    @Override
    public void createFile(Path path) {
        if (!path.isAbsolute()) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        Optional<BoxFsNode> foundDrive = findDrive(path);

        if (path.getNameCount() < 1) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        foundDrive.ifPresent(
                drive -> drive.createFile(
                        path.subpath(0, path.getNameCount())
                )
        );
    }

    @Override
    public void delete(Path path) {
        if (!path.isAbsolute()) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        Optional<BoxFsNode> foundDrive = findDrive(path);

        if (path.getNameCount() < 1) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        foundDrive.ifPresent(
                drive -> drive.delete(
                        path.subpath(0, path.getNameCount())
                )
        );
    }

    @Override
    public boolean exists(Path path) {
        if (!path.isAbsolute()) {
            return false;
        }

        String absolutePath = path.toString();

        char driveLetter = parseDriveLetter(absolutePath);

        if (path.getNameCount() < 1) {
            return drives.containsKey(driveLetter);
        }

        if (!drives.containsKey(driveLetter)) {
            return false;
        }

        return drives.get(driveLetter).exists(path.subpath(0, path.getNameCount()));
    }

    @Override
    public boolean isDirectory() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isDirectory(Path path) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isFile() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isFile(Path path) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Optional<BoxFsNode> readNode(Path path) {
        if (!path.isAbsolute()) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        Optional<BoxFsNode> foundDrive = findDrive(path);

        if (path.getNameCount() < 1) {
            return foundDrive;
        }

        return foundDrive
                .flatMap(drive -> drive.readNode(path.subpath(0, path.getNameCount())));
    }

    @Override
    public void writeContent(Path path, ByteBuffer buffer) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public <A extends BasicFileAttributes> A attributes() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public byte[] content() throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public <V extends FileAttributeView> V fileAttributeView() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Collection<String> children() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Optional<BoxFsNode> parent() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BoxFsPath path() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Iterable<Path> rootDirectories() {
        return drives.values()
                .stream()
                .map(BoxFsDrive::path)
                .collect(Collectors.toList());
    }

    private Optional<BoxFsNode> findDrive(Path path) {
        String absolutePath = path.toString();

        char driveLetter = parseDriveLetter(absolutePath);

        return Optional.ofNullable(drives.get(driveLetter));
    }

    private static char parseDriveLetter(String absolutePath) {
        Matcher matcher = DRIVE_LETTER_PATTERN.matcher(absolutePath);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid path: " + absolutePath);
        }

        String driveLetter = matcher.group("driveletter");
        if (driveLetter == null || driveLetter.length() != 1) {
            throw new IllegalArgumentException("Invalid path: " + absolutePath);
        }

        return driveLetter.charAt(0);
    }
}
