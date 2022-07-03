package net.azisaba.gravenbuilder;

import net.azisaba.gravenbuilder.util.GravenBuilderUtil;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class GravenBuilderConfig {
    @Internal URI dockerHost = GravenBuilderUtil.noThrow(() -> new URI("tcp://localhost:2375"));
    @Internal long timeout = 10;
    @Internal TimeUnit timeoutUnit = TimeUnit.MINUTES;
    @Internal Consumer<String> onStdout = frame -> {};
    @Internal Consumer<String> onStderr = frame -> {};
    @Internal Consumer<String> onDebug = msg -> {};
    @Internal Predicate<File> isArtifact = file -> file.getName().endsWith(".jar");

    @Contract(value = "_ -> this", mutates = "this")
    @NotNull
    public GravenBuilderConfig dockerHost(@NotNull String dockerHost) throws URISyntaxException {
        this.dockerHost = new URI(Objects.requireNonNull(dockerHost));
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    @NotNull
    public GravenBuilderConfig dockerHost(@NotNull URI dockerHost) {
        this.dockerHost = Objects.requireNonNull(dockerHost);
        return this;
    }

    /**
     * Sets the timeout for an actual build step. This does not apply for pulling images, etc.
     * @param timeout The timeout
     * @param timeoutUnit The timeout unit
     * @return this
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    @NotNull
    public GravenBuilderConfig timeout(@Range(from = 1, to = Long.MAX_VALUE) long timeout, @NotNull TimeUnit timeoutUnit) {
        //noinspection ConstantConditions // we want to check at runtime
        if (timeout < 1) {
            throw new IllegalArgumentException("timeout must be greater than 0");
        }
        this.timeout = timeout;
        this.timeoutUnit = Objects.requireNonNull(timeoutUnit);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    @NotNull
    public GravenBuilderConfig onStdout(@NotNull Consumer<@NotNull String> onStdout) {
        this.onStdout = Objects.requireNonNull(onStdout);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    @NotNull
    public GravenBuilderConfig onStderr(@NotNull Consumer<@NotNull String> onStderr) {
        this.onStderr = Objects.requireNonNull(onStderr);
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    @NotNull
    public GravenBuilderConfig onDebug(@NotNull Consumer<@NotNull String> onDebug) {
        this.onDebug = Objects.requireNonNull(onDebug);
        return this;
    }

    /**
     * Sets the predicate which determines whether a file is an artifact. By default, it only accepts files with the
     * extension ".jar".
     * @param isArtifact The predicate. {@link File#isFile()} is always true, and passes the file which was modified
     *                   <b>AFTER</b> the build has started.
     * @return this
     */
    @Contract(value = "_ -> this", mutates = "this")
    @NotNull
    public GravenBuilderConfig isArtifact(@NotNull Predicate<@NotNull File> isArtifact) { // file is always file, not directory.
        this.isArtifact = Objects.requireNonNull(isArtifact);
        return this;
    }
}
