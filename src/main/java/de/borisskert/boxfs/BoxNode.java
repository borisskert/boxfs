package de.borisskert.boxfs;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BoxNode {

    private final Map<String, BoxNode> children = new ConcurrentHashMap<>();

    private final String separator;

    private BoxNode(String separator) {
        this.separator = separator;
    }

    private BoxNode(BoxNode parent) {
        this.separator = parent.separator;
    }

    public void create(Path path) {
        children.putIfAbsent(
                path.getName(0).toString(),
                new BoxNode(this)
        );

        if (path.getNameCount() > 1) {
            children.get(path.getName(0).toString())
                    .create(
                            path.subpath(1, path.getNameCount())
                    );
        }
    }

    public void delete(Path path) {
        if (path.getNameCount() < 1) {
            return;
        }

        if (path.getNameCount() == 1) {
            children.remove(path.getName(0).toString());
            return;
        }

        children.get(
                path.getName(0).toString()
        ).delete(
                path.subpath(1, path.getNameCount())
        );
    }

    public boolean exists(Path path) {
        if (path.getNameCount() < 1) {
            return false;
        }

        if (path.getNameCount() == 1) {
            return children.containsKey(path.getName(0).toString());
        }

        return children.containsKey(path.getName(0).toString())
                && children.get(path.getName(0).toString())
                .exists(path.subpath(1, path.getNameCount()));
    }

    public static BoxNode newTree(String separator) {
        return new BoxNode(separator);
    }
}
