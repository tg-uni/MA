package de.uniba.pi.applicationsearcher.serverlessinfocollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uniba.pi.applicationsearcher.Main;
import de.uniba.pi.applicationsearcher.githubcollector.utils.GitHubRepositoryImplRaw;
import de.uniba.pi.applicationsearcher.model.HttpResponse;
import de.uniba.pi.applicationsearcher.model.ProjectInfo;
import de.uniba.pi.applicationsearcher.serverlessinfocollector.yamls.Serverless;
import de.uniba.pi.applicationsearcher.serverlessinfocollector.yamls.YamlRead;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ServerlessInfoCollector {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final String PROJECT_INFO_WITH_SERVERLESS_SUB_DIR = "projectInfoWithServerless";

    public static void addServerlessFileInfo(GitHubRepositoryImplRaw gitHubRepositoryImplRaw, Path directory) {
        Path projectInfosSubdir = directory.resolve(Main.PROJECT_INFO_SUB_DIR);
        try (Stream<Path> paths = Files.walk(projectInfosSubdir)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    ProjectInfo[] elements = gson.fromJson(Files.readString(path), ProjectInfo[].class);
                    System.out.println("Adding serverless file content info " + path);
                    // for debugging: System.out.println(path + " elemente: " + elements.length);
                    List<ProjectInfo> projectInfosWithServerless = addServerlessFileInfoForFile(gitHubRepositoryImplRaw, List.of(elements));
                    Path outputDirectory = directory.resolve(PROJECT_INFO_WITH_SERVERLESS_SUB_DIR);
                    if(!Files.exists(outputDirectory)) {
                        Files.createDirectory(outputDirectory);
                    }
                    Path writePath = outputDirectory.resolve(path.getFileName());
                    Files.writeString(writePath, gson.toJson(projectInfosWithServerless));
                    System.out.println("Finished adding serverless file content info for " + writePath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<ProjectInfo> addServerlessFileInfoForFile(GitHubRepositoryImplRaw gitHubRepositoryImplRaw, List<ProjectInfo> projectInfos) throws IOException {
        AtomicInteger fileCounter = new AtomicInteger();
        projectInfos.forEach(projectInfo -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String downloadUrl = projectInfo.getServerlessFileInfo().getDownloadUrl();
            try {
                HttpResponse contentOfUrl = gitHubRepositoryImplRaw.getContentOfUrl(downloadUrl);
                Optional<Serverless> serverless = YamlRead.readServerlessYaml(contentOfUrl.getBody(), projectInfo.getServerlessFileInfo().getHtmlUrl());
                serverless.ifPresent(value -> {
                    if (value.getProvider() != null) {
                        projectInfo.getServerlessFileInfo().setRuntime(value.getProvider().getRuntime());
                    }
                    projectInfo.getServerlessFileInfo().setServerlessVersion(value.getFrameworkVersion());
                    projectInfo.getServerlessFileInfo().setPlugins(value.getPlugins());
                    fileCounter.getAndIncrement();
                });
            } catch (IOException e) {
                System.out.println("Could not get file from " + downloadUrl + "\n Index: " + fileCounter.get() + "\n Exception: " + e.getMessage());
            }
        });
        return projectInfos;
    }
}
