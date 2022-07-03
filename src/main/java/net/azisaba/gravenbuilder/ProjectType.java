package net.azisaba.gravenbuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ProjectType {
    public static final @NotNull ProjectType GRADLE = new ProjectType("gradle", "openjdk:%d", "/bin/bash", "-c", "./gradlew --project-cache-dir /tmp/.gradle_cache build --stacktrace --info");
    public static final @NotNull ProjectType MAVEN = new ProjectType("maven", "maven:3-eclipse-temurin-%d-focal", "/bin/bash", "-c", "mvn package");

    private final String name;
    private final String image;
    private final String[] cmd;

    private ProjectType(@NotNull String name, @NotNull String image, @NotNull String @NotNull ... cmd) {
        this.name = name;
        this.image = image;
        this.cmd = cmd;
    }

    @Contract(pure = true)
    public @NotNull String getImage() {
        return image;
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
        return withCustomCmd("openjdk:17", cmd);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ProjectType withCustomCmd(@NotNull String image, @NotNull String @NotNull ... cmd) {
        return new ProjectType("custom", image, cmd);
    }

    @Override
    public @NotNull String toString() {
        return "ProjectType{name=" + name + ", image=" + image + ", cmd=[" + String.join(", ", cmd) + "]}";
    }
}
