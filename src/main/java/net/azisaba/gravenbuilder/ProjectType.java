package net.azisaba.gravenbuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ProjectType {
    public static final @NotNull ProjectType GRADLE = new ProjectType("gradle", "/bin/bash", "-c", "./gradlew --project-cache-dir /tmp/.gradle_cache build --stacktrace --info");
    public static final @NotNull ProjectType MAVEN = new ProjectType("maven", "/bin/bash", "-c", "mvn package");

    private final String name;
    private final String[] cmd;

    private ProjectType(@NotNull String name, @NotNull String @NotNull ... cmd) {
        this.name = name;
        this.cmd = cmd;
    }

    @Contract(pure = true)
    public @NotNull String @NotNull [] getCmd() {
        return cmd;
    }

    @Contract
    public @NotNull String name() {
        return name;
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ProjectType withCustomCmd(@NotNull String @NotNull ... cmd) {
        return new ProjectType("custom", cmd);
    }

    @Override
    public @NotNull String toString() {
        return "ProjectType{name=" + name + ", cmd=[" + String.join(", ", cmd) + "]}";
    }
}
