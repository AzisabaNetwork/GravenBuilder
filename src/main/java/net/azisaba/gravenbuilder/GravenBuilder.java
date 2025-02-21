package net.azisaba.gravenbuilder;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import net.azisaba.gravenbuilder.util.GravenBuilderUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GravenBuilder {
    private final GravenBuilderConfig config;
    private final DockerClient client;

    public GravenBuilder(@NotNull GravenBuilderConfig config) {
        this.config = config;
        var dockerConfig = new DefaultDockerClientConfig.Builder().withDockerHost(config.dockerHost.toString()).build();
        var httpClient = new ApacheDockerHttpClient.Builder().dockerHost(config.dockerHost).build();
        this.client = DockerClientImpl.getInstance(dockerConfig, httpClient);
    }

    /**
     * Builds the project. Project type is determined by the project directory, and throws an exception if the project
     * type is not supported, or both gradle and maven files were found and cannot determine the project type.
     * @param path the project base path.
     * @param javaVersion the java version.
     * @return artifacts (*.jar)
     * @throws InterruptedException if the thread is interrupted.
     * @throws RuntimeException if project type could not be determined.
     * @see #buildOn(File, int, ProjectType) to explicitly specify the project type.
     */
    @NotNull
    public List<File> buildOn(@NotNull File path, int javaVersion) throws InterruptedException {
        return buildOn(path, javaVersion, null);
    }

    /**
     * Builds the project.
     * @param path the project base path.
     * @param javaVersion the java version.
     * @return artifacts (*.jar)
     * @throws InterruptedException if the thread is interrupted.
     * @throws RuntimeException if <code>overrideProjectType</code> is null and project type could not be determined.
     */
    @NotNull
    public List<File> buildOn(@NotNull File path, int javaVersion, @Nullable ProjectType overrideProjectType) throws InterruptedException {
        var projectType = Objects.requireNonNullElseGet(overrideProjectType, () -> detectProjectType(path));
        config.onDebug.accept("Using project type: " + projectType);
        Volume app = new Volume("/app");
        Volume homeMaven = new Volume("/root/.m2");
        Volume homeGradle = new Volume("/root/.gradle");
        String imageName = String.format(projectType.getImage(), javaVersion);
        client.pullImageCmd(imageName).exec(new ResultCallback.Adapter<>() {
            private long time = System.currentTimeMillis();

            @Override
            public void onNext(PullResponseItem object) {
                if (System.currentTimeMillis() - time < 250) {
                    return;
                }
                time = System.currentTimeMillis();
                var detail = object.getProgressDetail();
                if (detail == null) {
                    return;
                }
                double percentage;
                if (detail.getTotal() == null) {
                    percentage = 0;
                } else {
                    percentage = toDouble(detail.getCurrent()) / toDouble(detail.getTotal());
                }
                Object maybeCurrent = Objects.requireNonNullElse(detail.getCurrent(), "?");
                Object maybeTotal = Objects.requireNonNullElse(detail.getTotal(), "?");
                config.onDebug.accept("Pulling layer " + object.getId() + " (status: " + object.getStatus() + "): " + percentage + "% (" + maybeCurrent + "/" + maybeTotal + ")");
            }

            @Override
            public void onComplete() {
                config.onDebug.accept("Successfully pulled image '" + imageName + "'");
                super.onComplete();
            }

            private static double toDouble(@Nullable Long l) {
                return l == null ? 0 : l.doubleValue();
            }
        }).awaitCompletion();
        config.onDebug.accept("Starting build");
        CreateContainerResponse container = client.createContainerCmd(imageName)
                .withVolumes(app, homeMaven, homeGradle)
                .withHostConfig(HostConfig.newHostConfig().withBinds(
                        new Bind("/tmp/gravenbuilder-home-maven", homeMaven),
                        new Bind("/tmp/gravenbuilder-home-gradle", homeGradle),
                        new Bind(path.getAbsolutePath(), app)
                ))
                .withWorkingDir("/app")
                .withCmd(projectType.getCmd())
                .exec();
        config.onDebug.accept("Created container: " + container.getId());
        try {
            long timeBeforeStart = System.currentTimeMillis();
            client.startContainerCmd(container.getId()).exec();
            config.onDebug.accept("Started container: " + container.getId());
            var timedOut = client.logContainerCmd(container.getId()).withStdOut(true).withStdErr(true).withTail(100).withFollowStream(true).exec(new ResultCallback.Adapter<>() {
                @Override
                public void onNext(Frame frame) {
                    if (frame.getStreamType() == StreamType.STDOUT) {
                        config.onStdout.accept(new String(frame.getPayload()).trim());
                    } else if (frame.getStreamType() == StreamType.STDERR) {
                        config.onStderr.accept(new String(frame.getPayload()).trim());
                    }
                }
            }).awaitCompletion(config.timeout, config.timeoutUnit);
            if (!timedOut) {
                config.onDebug.accept("Terminated due to exceeding timeout of " + config.timeout + " " + config.timeoutUnit.name());
            }
            List<File> artifacts = new ArrayList<>();
            for (File file : GravenBuilderUtil.findAllFiles(path)) {
                if (file.lastModified() > timeBeforeStart && config.isArtifact.test(file)) {
                    artifacts.add(file);
                }
            }
            return artifacts;
        } finally {
            client.removeContainerCmd(container.getId()).withRemoveVolumes(true).withForce(true).exec();
            config.onDebug.accept("Removed container: " + container.getId());
        }
    }

    @NotNull
    private ProjectType detectProjectType(@NotNull File baseDir) throws RuntimeException {
        File gradlew = new File(baseDir, "gradlew");
        File pom = new File(baseDir, "pom.xml");
        if (gradlew.exists() && gradlew.isFile() && pom.exists() && pom.isFile()) {
            throw new RuntimeException("Both gradlew and pom.xml are found in " + baseDir.getAbsolutePath() + " (please specify the project type explicitly)");
        }
        if (gradlew.exists() && gradlew.isFile()) {
            return ProjectType.GRADLE;
        }
        if (pom.exists() && pom.isFile()) {
            return ProjectType.MAVEN;
        }
        throw new RuntimeException("Neither gradlew nor pom.xml is found in " + baseDir.getAbsolutePath());
    }
}
