package de.borisskert.boxfs.windows;

import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BoxFsPaths {
    private static final String SEPARATOR = "\\";
    private static final String CURRENT_WORKING_DIRECTORY = "C:";

    private BoxFsPaths() {
        // utility class
    }

    public static String parentOf(String path) {
        if (!isAbsolute(path)) {
            return null;
        }

        String[] parts = path.split("\\\\");

        if (parts.length == 2) {
            return parts[0] + SEPARATOR;
        }

        if (parts.length == 1) {
            return null;
        }

        String[] parentParts = Arrays.copyOf(parts, parts.length - 1);
        return String.join(SEPARATOR, parentParts);
    }

    public static boolean isAbsolute(String path) {
        String nonNullPath = Objects.requireNonNull(path);
        String validPath = requireValidPath(nonNullPath);

        return validPath
                .matches("^[A-Za-z]:\\\\(.*)?");
    }

    private static final Pattern ABSOLUTE_PATH_PATTERN = Pattern.compile("^[A-Za-z]:(\\\\(?<relativepath>.*)?)?$");

    private static String requireValidPath(String path) {
        if (path == null) {
            return null;
        }

        if (path.startsWith(SEPARATOR + SEPARATOR)) {
            throw new InvalidPathException(path, "Is invalid");
        }

        Matcher matcher = ABSOLUTE_PATH_PATTERN.matcher(path);
        if (matcher.matches()) { // is absolute path
            String relativepath = matcher.group("relativepath");

            if (relativepath != null && relativepath.contains(":")) {
                throw new InvalidPathException(path, "Is invalid");
            }
        } else { // is relative path
            if (path.contains(":")) {
                throw new InvalidPathException(path, "Is invalid");
            }
        }

        return path;
    }

    private static String relativePathOnly(String path) {
        Matcher matcher = ABSOLUTE_PATH_PATTERN.matcher(path);

        if (matcher.matches()) {
            String relativepath = matcher.group("relativepath");
            if (relativepath == null) {
                return "";
            } else {
                return relativepath;
            }
        }

        return path;
    }

    public static String relativize(String path, String other) {
        path = normalize(path);
        other = normalize(other);

        String[] a = path.split("\\\\");
        String[] b = other.split("\\\\");

        boolean pathIsFile = a.length > 0 && a[a.length - 1].contains(".");

        // find common prefix
        int i = 0;
        int max = Math.min(a.length, b.length);
        while (i < max && a[i].equals(b[i])) {
            i++;
        }

        // same directory
        if (i == a.length && i == b.length) {
            return pathIsFile ? ".." : "";
        }

        // other is inside path
        if (i == a.length && b.length > a.length) {
            return String.join("\\", Arrays.copyOfRange(b, i, b.length));
        }

        // path is inside other
        if (i == b.length && a.length > b.length) {
            int up = a.length - i;
            return String.join("\\", Collections.nCopies(up, ".."));
        }

        // general case
        int up = a.length - i;
        String ups = String.join("\\", Collections.nCopies(up, ".."));
        String down = String.join("\\", Arrays.copyOfRange(b, i, b.length));

        if (ups.isEmpty()) return down;
        if (down.isEmpty()) return ups;

        return ups + "\\" + down;
    }

    private static String normalize(String p) {
        if (p.endsWith("\\")) {
            return p.substring(0, p.length() - 1);
        }
        return p;
    }

    private static final Pattern RELATIVE_PATH_PATTERN = Pattern.compile("^(\\\\)+(?<path>[A-Za-z0-9._-].*)$");

    public static String toAbsolutePath(String path) {
        path = Objects.requireNonNull(path);

        if (path.isEmpty()) {
            return CURRENT_WORKING_DIRECTORY;
        }

        if (path.startsWith(SEPARATOR + SEPARATOR)) {
            return nonEndingSeparator(path);
        }

        if (isAbsolute(path)) {
            return path;
        }

        path = nonStartingSeparator(path);
        path = nonEndingSeparator(path);

        return CURRENT_WORKING_DIRECTORY + SEPARATOR + path;
    }

    private static String nonStartingAndEndingSeparator(String path) {
        return nonEndingSeparator(nonStartingSeparator(path));
    }

    private static String nonStartingSeparator(String path) {
        if (path == null) {
            return null;
        }

        Matcher matcher = RELATIVE_PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return matcher.group("path");
        }

        return path;
    }

    private static String nonEndingSeparator(String path) {
        if (path == null) {
            return null;
        }

        Matcher matcher = ENDING_SLASH.matcher(path);
        if (matcher.matches()) {
            return matcher.group("path");
        }

        return path;
    }

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[A-Za-z]:\\\\(.+\\\\)?(?<fileName>[^\\\\]+)$");
    private static final Pattern ENDING_SLASH = Pattern.compile("^(?<path>.*)\\\\$");

    public static String getFileName(String path) {
        path = Objects.requireNonNull(path);
        path = nonEndingSeparator(path);

        Matcher matcher = FILENAME_PATTERN.matcher(path);

        if (matcher.matches()) {
            return matcher.group("fileName");
        }

        return path;
    }

    private static final Pattern DRIVE_LETTER_PATTERN = Pattern.compile("^(?<driveletter>[A-Za-z]:)(\\\\.*)?");
    private static final Pattern ROOT_PATH_PATTERN = Pattern.compile("^([A-Za-z]:)(\\\\)?");

    public static String getRoot(String path) {
        path = Objects.requireNonNull(path);

        if (!ABSOLUTE_PATH_PATTERN.matcher(path).matches()) {
            return null;
        }

        if (ROOT_PATH_PATTERN.matcher(path).matches()) {
            return path;
        }

        Matcher matcher = DRIVE_LETTER_PATTERN.matcher(path);
        if (matcher.matches()) {
            return matcher.group("driveletter") + "\\";
        }

        throw new IllegalArgumentException("Invalid path: " + path);
    }

    public static int getNameCount(String path) {
        path = Objects.requireNonNull(path);

        if (path.isEmpty()) {
            return 1;
        }

        String[] parts = path.split("\\\\");

        if (isAbsolute(path)) {
            return parts.length - 1;
        }

        return parts.length;
    }

    public static String getName(String path, int index) {
        path = Objects.requireNonNull(path);

        if (isAbsolute(path)) {
            index += 1;
        }

        String[] parts = path.split("\\\\");

        if (index < parts.length) {
            return parts[index];
        }

        throw new IllegalArgumentException("Invalid index for getName");
    }

    public static String subpath(String path, int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > getNameCount(path)) {
            throw new IllegalArgumentException("Invalid begin or end index for subpath");
        }

        if (path == null) {
            return null;
        }

        path = nonStartingSeparator(path);

        String[] parts = path.split("\\\\");
        if (isAbsolute(path)) {
            beginIndex += 1;
            endIndex += 1;
        }

        if (beginIndex >= 0 && endIndex <= parts.length && beginIndex < endIndex) {
            String[] subParts = Arrays.copyOfRange(parts, beginIndex, endIndex);
            return String.join(SEPARATOR, subParts);
        }

        if (beginIndex == endIndex) {
            throw new IllegalArgumentException("Invalid begin or end index for subpath");
        }

        throw new IllegalArgumentException("Invalid begin or end index for subpath");
    }

    public static String resolve(String path, String other) {
        if (other == null) {
            return path;
        }

        if (isAbsolute(other)) {
            return other;
        }

        if (isAbsolute(path)) {
            if (other.startsWith(SEPARATOR)) {
                return getRoot(path) + nonStartingAndEndingSeparator(other);
            }

            return nonEndingSeparator(path) + SEPARATOR + nonStartingAndEndingSeparator(other);
        }

        if (other.startsWith(SEPARATOR)) {
            return other;
        }

        return nonStartingAndEndingSeparator(path) + SEPARATOR + nonStartingAndEndingSeparator(other);
    }

    public static java.util.Iterator<String> iterator(String path) {
        path = Objects.requireNonNull(path);
        path = requireValidPath(path);

        if (isDriveLetterOnly(path)) {
            return Collections.emptyIterator();
        }

        if (isAbsolute(path)) {
            path = relativePathOnly(path);

            if (path.isEmpty()) {
                return Collections.emptyIterator();
            }
        }

        path = nonStartingAndEndingSeparator(path);

        String[] parts = path.split("\\\\");

        return Arrays.asList(parts).iterator();
    }

    private static boolean isDriveLetterOnly(String path) {
        Matcher matcher = DRIVE_LETTER_PATTERN.matcher(path);
        return matcher.matches() && (matcher.group(2) == null || matcher.group(2).isEmpty());
    }
}
