package net.azisaba.gravenbuilder.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GravenBuilderUtil {
    @Contract(pure = true)
    public static <T> T noThrow(@NotNull ThrowableSupplier<T, ? extends Throwable> supplier) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw new RuntimeException(t);
        }
    }

    @Unmodifiable
    @NotNull
    public static List<File> findAllFiles(@NotNull File baseDir) {
        if (!baseDir.exists()) {
            return List.of();
        }
        if (baseDir.isFile()) {
            return List.of(baseDir);
        }
        List<File> list = new ArrayList<>();
        File[] files = baseDir.listFiles();
        if (files == null) {
            return List.of();
        }
        for (File file : files) {
            if (file.isDirectory()) {
                list.addAll(findAllFiles(file));
            } else {
                list.add(file);
            }
        }
        return Collections.unmodifiableList(list);
    }
}
