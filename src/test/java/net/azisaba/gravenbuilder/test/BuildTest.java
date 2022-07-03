package net.azisaba.gravenbuilder.test;

import net.azisaba.gravenbuilder.GravenBuilder;
import net.azisaba.gravenbuilder.GravenBuilderConfig;

import java.io.File;

public class BuildTest {
    // builds itself and outputs the artifacts as result
    public static void main(String[] args) throws InterruptedException {
        File cwd = new File(".").getAbsoluteFile();
        System.out.println("Current working directory: " + cwd);
        var builder = new GravenBuilder(new GravenBuilderConfig()
                .onStdout(msg -> System.out.println("[STDOUT] " + msg))
                .onStderr(msg -> System.err.println("[STDERR] " + msg))
                .onDebug(msg -> System.out.println("[DEBUG] " + msg))
        );
        for (File file : builder.buildOn(cwd, 17)) {
            System.out.println("Artifact: " + file);
        }
    }
}
