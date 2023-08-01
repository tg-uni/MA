package de.uniba.pi.applicationsearcher.githubcollector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uniba.pi.applicationsearcher.Main;
import de.uniba.pi.applicationsearcher.githubcollector.utils.GitHubRepositoryImplRaw;
import de.uniba.pi.applicationsearcher.githubcollector.utils.PaginationHelper;
import de.uniba.pi.applicationsearcher.githubcollector.utils.RepositoryDataExtractor;
import de.uniba.pi.applicationsearcher.model.HttpResponse;
import de.uniba.pi.applicationsearcher.model.ProjectInfo;
import de.uniba.pi.applicationsearcher.githubcollector.githubapi.GithubCodeSearchResult;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class GithubCollector {


    public static final String FILENAME_BASE = "searchResult_";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static void collectGithubResults(GitHubRepositoryImplRaw gitHubRepositoryImplRaw, Path directoryPath, int maxNumberOfPages, int resultsPerPage) throws IOException {
        // Make directories for result files
        Files.createDirectory(directoryPath.resolve(Main.SEARCH_RESULT_SUB_DIR));
        Files.createDirectory(directoryPath.resolve(Main.PROJECT_INFO_SUB_DIR));
        // Search files
        String[] searchKeys = {"aws", "handler", "filename:serverless.yml",
                "-cake-ordering-system", "-udacity", "-udagram", "-todo-app", "-serverless-todo-app",
                "-serverless-todos-app", "-path:course-04/"
        };
        String searchQuery = Arrays.stream(searchKeys).map(key -> URLEncoder.encode(key, StandardCharsets.UTF_8)).collect(Collectors.joining("%20"));
        String nextPageUrl = String.format("https://api.github.com/search/code?page=%d&per_page=%d&q=%s", 1, resultsPerPage,
                searchQuery);
        for (int pageNumber = 1; pageNumber <= maxNumberOfPages && nextPageUrl != null; pageNumber++) {
            System.out.println("Starting for Page number: " + pageNumber);
            Path searchResultFilePath = directoryPath.resolve(Main.SEARCH_RESULT_SUB_DIR).resolve(FILENAME_BASE + (pageNumber) + ".json");
            nextPageUrl = getAndSaveSearchResults(gitHubRepositoryImplRaw, searchResultFilePath, nextPageUrl);
            // Add information for repositories
            RepositoryDataExtractor repositoryDataExtractor = new RepositoryDataExtractor();
            ArrayList<ProjectInfo> informationOfProjects = repositoryDataExtractor.getInformationOfRepositories(searchResultFilePath);
            Path withRepositoryInfoFilePath = directoryPath.resolve(Main.PROJECT_INFO_SUB_DIR).resolve("projectInfo_" + pageNumber + ".json");
            Files.writeString(withRepositoryInfoFilePath, gson.toJson(informationOfProjects));
            System.out.println("Saved repository information for " + searchResultFilePath + " to " + withRepositoryInfoFilePath);
        }
    }

    public static String getAndSaveSearchResults(GitHubRepositoryImplRaw gitHubRepositoryImplRaw, Path filePath, String url) throws IOException {
        String nextPageLink = null;
        try {
            System.out.println("Current page search url: " + url);
            HttpResponse result = gitHubRepositoryImplRaw.getContentOfUrl(url);
            nextPageLink = PaginationHelper.getNextPageLink(result);
            String resultContent = result.getBody();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            GithubCodeSearchResult githubCodeSearchResult = gson.fromJson(resultContent, GithubCodeSearchResult.class);
            Files.writeString(filePath, gson.toJson(githubCodeSearchResult));
            System.out.println("Saved search results to file " + filePath);
            System.out.println("Pausing...");
            Thread.sleep(100000);
        } catch (IOException e) {
            System.err.println("URL: " + url + " could not be fetched");
            System.err.println(e.getMessage());
            try {
                Thread.sleep(300000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return nextPageLink;
    }
}
