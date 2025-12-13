package de.borisskert.boxfs.windows;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BoxFsPaths {
    private static final String SEPARATOR = "\\";

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

        String[] parentParts = Arrays.copyOf(parts, parts.length - 1);
        return String.join(SEPARATOR, parentParts);
    }

    public static boolean isAbsolute(String path) {
        if (path == null) {
            return false;
        }

        return path.matches("^[A-Za-z]:(\\\\.*)?");
    }

    public static String relativize(String path, String other) {
        String[] a = path.split("\\\\");
        String[] b = other.split("\\\\");

        // find common prefix length
        int i = 0;
        int max = Math.min(a.length, b.length);
        while (i < max && a[i].equals(b[i])) {
            i++;
        }

        // if 'other' is within 'path': return the remainder from 'other'
        if (i == a.length && b.length > a.length) {
            String[] remainder = Arrays.copyOfRange(b, i, b.length);
            return String.join(SEPARATOR, remainder);
        }

        // if 'path' is within 'other'
        if (i == b.length && a.length > b.length) {
            String last = a[a.length - 1];
            // if last looks like a file (has an extension), return file name
            if (last.contains(".")) {
                return last;
            }

            int up = a.length - i;
            String[] ups = new String[up];
            Arrays.fill(ups, "..");
            return String.join(SEPARATOR, ups);
        }

        // default: build relative from 'path' to 'other'
        int up = a.length - i;
        String[] ups = new String[up];
        Arrays.fill(ups, "..");
        String[] down = Arrays.copyOfRange(b, i, b.length);

        if (down.length == 0) {
            return String.join(SEPARATOR, ups);
        }

        if (ups.length == 0) {
            return String.join(SEPARATOR, down);
        }

        return String.join(SEPARATOR, ups) + SEPARATOR + String.join(SEPARATOR, down);
    }

    private static final Pattern RELATIVE_PATH_PATTERN = Pattern.compile("^(\\\\)+(?<path>[A-Za-z0-9._-].*)$");

    public static String toAbsolutePath(String path) {
        if (path == null) {
            return null;
        }

        if (isAbsolute(path)) {
            return path;
        }

        Matcher matcher = RELATIVE_PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            return "C:\\" + matcher.group("path");
        }

        return "C:\\" + path;
    }

    private static String normalizePath(String path) {
        String normalized = path;

        Matcher matcher = RELATIVE_PATH_PATTERN.matcher(path);
        if (matcher.matches()) {
            normalized = matcher.group("path");
        }

        matcher = ENDING_SLASH.matcher(path);
        if (matcher.matches()) {
            normalized = matcher.group("path");
        }

        if (normalized.equals(path)) {
            return path;
        }

        return normalizePath(normalized);
    }

    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[A-Za-z]:\\\\(.+\\\\)?(?<fileName>[^\\\\]+)$");
    private static final Pattern ENDING_SLASH = Pattern.compile("^(?<path>.*)\\\\$");

    public static String getFileName(String path) {
        if (path == null) {
            return null;
        }

        if (ENDING_SLASH.matcher(path).matches()) {
            return "";
        }

        Matcher matcher = FILENAME_PATTERN.matcher(path);

        if (matcher.matches()) {
            return matcher.group("fileName");
        }

        return path;
    }

    private static final Pattern DRIVE_LETTER_PATTERN = Pattern.compile("^(?<driveletter>[A-Za-z]:)(\\\\.*)?");

    public static String getRoot(String path) {
        if (path == null) {
            return null;
        }

        if (!isAbsolute(path)) {
            return null;
        }

        Matcher matcher = DRIVE_LETTER_PATTERN.matcher(path);
        if (matcher.matches()) {
            return matcher.group("driveletter") + "\\";
        }

        throw new IllegalArgumentException("Invalid path: " + path);
    }

    public static int getNameCount(String path) {
        if (path == null || path.isEmpty()) {
            return 0;
        }

        String[] parts = path.split("\\\\");

        if (isAbsolute(path)) {
            return parts.length - 1;
        }

        return parts.length;
    }

    public static String getName(String path, int index) {
        if (path == null) {
            return null;
        }

        if (isAbsolute(path)) {
            index += 1;
        }

        String[] parts = path.split("\\\\");

        if (index < parts.length) {
            return parts[index];
        }

        return null;
    }

    public static String subpath(String path, int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > getNameCount(path)) {
            throw new IllegalArgumentException("Invalid begin or end index for subpath");
        }

        if (path == null) {
            return null;
        }

        path = normalizePath(path);

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
            return "";
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

        return normalizePath(path) + SEPARATOR + normalizePath(other);
    }
}
