package de.borisskert.boxfs.windows;

import java.nio.file.attribute.BasicFileAttributes;

/**
 * Debugging wrapper for {@link BasicFileAttributes} that provides additional functionality for testing purposes.
 * We do not want to make {@link BoxFsDebugBasicFileAttributes} public so we use this class as a wrapper.
 */
public class BoxFsTestingBasicFileAttributes extends BoxFsDebugBasicFileAttributes {

    BoxFsTestingBasicFileAttributes(BasicFileAttributes delegate, Object fileKey) {
        super(delegate, fileKey);
    }
}
