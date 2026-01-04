package de.borisskert.boxfs.macos;

import java.nio.file.CopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

class CopyOptions {
    private final Set<CopyOption> options;

    private CopyOptions(Set<CopyOption> options) {
        this.options = options;
    }

    public boolean replaceExisting() {
        return this.options.contains(REPLACE_EXISTING);
    }

    public boolean atomicMove() {
        return this.options.contains(ATOMIC_MOVE);
    }

    public static CopyOptions of(CopyOption... options) {
        Set<CopyOption> optionsAsSet = new HashSet<>();
        Collections.addAll(optionsAsSet, options);

        return new CopyOptions(Collections.unmodifiableSet(optionsAsSet));
    }
}
