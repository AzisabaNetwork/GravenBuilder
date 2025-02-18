package net.azisaba.gravenbuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class ProjectType {
    public static final @NotNull ProjectType GRADLE = new ProjectType("gradle", "eclipse-temurin:%d-jdk", "/bin/bash", "-c", "./gradlew build --stacktrace --info");
    public static final @NotNull ProjectType MAVEN = new ProjectType("maven", "maven:3-eclipse-temurin-%d", "/bin/bash", "-c", "mvn package");

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

    /**
     * @deprecated Use {@link #withCustomImageCmd(String, String...)} instead
     * @param cmd
     * @return
     */
    @Deprecated
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull ProjectType withCustomCmd(@NotNull String @NotNull ... cmd) {
        return withCustomImageCmd("openjdk:17", cmd);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull ProjectType withCustomImageCmd(@NotNull String image, @NotNull String @NotNull ... cmd) {
        return new ProjectType("custom", image, cmd);
    }

    @Override
    public @NotNull String toString() {
        return "ProjectType{name=" + name + ", image=" + image + ", cmd=[" + String.join(", ", cmd) + "]}";
    }
}
