package de.borisskert.boxfs.macos;

import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BoxFsPaths {
    private static final String SEPARATOR = "/";
    private static final String CURRENT_WORKING_DIRECTORY = "/workingdirectory";

    private BoxFsPaths() {
        // utility class
    }

    public static String getParent(String path) {
        if (!isAbsolute(path)) {
            return null;
        }

        String[] parts = path.split(SEPARATOR);

        if (parts.length == 2) {
            return parts[0] + SEPARATOR;
        }

        if (parts.length == 1) {
            return null;
        }

        if (parts.length == 0) {
            return null;
        }

        String[] parentParts = Arrays.copyOf(parts, parts.length - 1);
        return String.join(SEPARATOR, parentParts);
    }

    public static boolean isAbsolute(String path) {
        String nonNullPath = Objects.requireNonNull(path);
        String validPath = requireValidPath(nonNullPath);

        return validPath
                .matches("^/(.*)?");
    }

    private static final Pattern ABSOLUTE_PATH_PATTERN = Pattern.compile("^/(?<relativepath>.*)?$");

    private static String requireValidPath(String path) {
        if (path == null) {
            return null;
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
        path = nonMultipleStartingSeparator(path);

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
        path = nonEndingSeparator(nonMultipleStartingSeparator(path));
        other = nonEndingSeparator(nonMultipleStartingSeparator(other));

        String[] a = path.split(SEPARATOR);
        String[] b = other.split(SEPARATOR);

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
            return String.join(BoxFsPaths.SEPARATOR, Arrays.copyOfRange(b, i, b.length));
        }

        // path is inside other
        if (i == b.length && a.length > b.length) {
            int up = a.length - i;
            return String.join(BoxFsPaths.SEPARATOR, Collections.nCopies(up, ".."));
        }

        // general case
        int up = a.length - i;
        String ups = String.join(BoxFsPaths.SEPARATOR, Collections.nCopies(up, ".."));
        String down = String.join(BoxFsPaths.SEPARATOR, Arrays.copyOfRange(b, i, b.length));

        if (ups.isEmpty()) return down;
        if (down.isEmpty()) return ups;

        return ups + BoxFsPaths.SEPARATOR + down;
    }

    private static final Pattern RELATIVE_PATH_PATTERN = Pattern.compile("^(/)+(?<path>[A-Za-z0-9._-].*)$");

    public static String toAbsolutePath(String path) {
        path = Objects.requireNonNull(path);

        if (path.isEmpty()) {
            return CURRENT_WORKING_DIRECTORY;
        }

        if (path.startsWith(SEPARATOR + SEPARATOR)) {
            return nonEndingSeparator(nonMultipleStartingSeparator(path));
        }

        if (isAbsolute(path)) {
            return nonEndingSeparator(path);
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

    private static final Pattern MULTIPLE_STARTING_SLASH_PATTERN = Pattern.compile("^/+(?<path>/.*)$");

    private static String nonMultipleStartingSeparator(String path) {
        if (path == null) {
            return null;
        }

        Matcher matcher = MULTIPLE_STARTING_SLASH_PATTERN.matcher(path);
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
            return nonEndingSeparator(matcher.group("path"));
        }

        return path;
    }

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^/(.+/)?(?<fileName>[^/]+)$");
    private static final Pattern ENDING_SLASH = Pattern.compile("^(?<path>.*)/$");

    public static String getFileName(String path) {
        path = Objects.requireNonNull(path);
        path = nonEndingSeparator(path);

        Matcher matcher = FILENAME_PATTERN.matcher(path);

        if (matcher.matches()) {
            return matcher.group("fileName");
        }

        return path;
    }

    public static String getRoot(String path) {
        path = Objects.requireNonNull(path);

        if (path.isEmpty()) {
            return null;
        }

        if (!isAbsolute(path)) {
            return null;
        }

        return "/";
    }

    public static int getNameCount(String path) {
        path = Objects.requireNonNull(path);

        if (path.isEmpty()) {
            return 1;
        }

        if (path.equals(SEPARATOR)) {
            return 0;
        }

        String[] parts = path.split(SEPARATOR);

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

        String[] parts = path.split(SEPARATOR);

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

        String[] parts = path.split(SEPARATOR);
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

        if (isAbsolute(path)) {
            path = relativePathOnly(path);

            if (path.isEmpty()) {
                return Collections.emptyIterator();
            }
        }

        path = nonStartingAndEndingSeparator(path);

        String[] parts = path.split(SEPARATOR);

        return Arrays.asList(parts).iterator();
    }

    public static String normalize(String path) {
        Objects.requireNonNull(path);

        if (path.isEmpty()) {
            return "";
        }

        boolean isAbsolute = path.startsWith("/");

        // Split on slashes (multiple slashes are ignored)
        String[] parts = path.split("/+");

        Deque<String> stack = new ArrayDeque<String>();

        for (String part : parts) {
            if (part.isEmpty() || ".".equals(part)) {
                continue;
            }

            if ("..".equals(part)) {
                if (!stack.isEmpty()) {
                    stack.removeLast();
                }
                continue;
            }

            stack.addLast(part);
        }

        String normalized = String.join("/", stack);

        if (isAbsolute) {
            normalized = "/" + normalized;
        }

        return normalized;
    }
}
