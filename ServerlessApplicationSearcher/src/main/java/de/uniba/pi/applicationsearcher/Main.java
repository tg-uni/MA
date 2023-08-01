package de.uniba.pi.applicationsearcher;

import de.uniba.pi.applicationsearcher.githubcollector.GithubCollector;
import de.uniba.pi.applicationsearcher.githubcollector.utils.GitHubRepositoryImplRaw;
import de.uniba.pi.applicationsearcher.serverlessinfocollector.ServerlessInfoCollector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static final GitHubRepositoryImplRaw gitHubRepositoryImplRaw = new GitHubRepositoryImplRaw(
            System.getenv("GITHUB_API_TOKEN"));


    public static final String DIRECTORY_BASE = "results";

    public static String SEARCH_RESULT_SUB_DIR = "searchResults";
    public static String PROJECT_INFO_SUB_DIR = "projectInfos";

    public static void main(String[] args) throws IOException {
        Path resultDirectory = createResultDirectory();
        int maxNumberOfPages = 10;
        int resultsPerPage = 100;
        System.out.println("Configuration:\n\tMax number of pages: " + maxNumberOfPages + "\n\tResults per page: "+resultsPerPage);
        System.out.println("###### Collecting search results and repository data ######");
        GithubCollector.collectGithubResults(gitHubRepositoryImplRaw, resultDirectory, maxNumberOfPages, resultsPerPage);
        System.out.println("###### Adding serverless file content info ######");
        ServerlessInfoCollector.addServerlessFileInfo(gitHubRepositoryImplRaw, resultDirectory);
    }

    public static Path createResultDirectory() throws IOException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        Path directoryPath = Path.of(DIRECTORY_BASE + "_" + dateTimeFormatter.format(LocalDateTime.now()));
        Files.createDirectory(directoryPath);
        return directoryPath;
    }



}
