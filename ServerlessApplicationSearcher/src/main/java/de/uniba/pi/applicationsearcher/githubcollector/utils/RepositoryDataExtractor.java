package de.uniba.pi.applicationsearcher.githubcollector.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

import de.uniba.pi.applicationsearcher.Main;
import de.uniba.pi.applicationsearcher.model.HttpResponse;
import de.uniba.pi.applicationsearcher.model.ProjectInfo;
import de.uniba.pi.applicationsearcher.model.ServerlessFileInfo;
import de.uniba.pi.applicationsearcher.githubcollector.githubapi.*;

public class RepositoryDataExtractor {

    Gson gson = new Gson();

    public ArrayList<ProjectInfo> getInformationOfRepositories(Path filePath) throws IOException {
        ArrayList<ProjectInfo> repositoriesInformation = new ArrayList<>();
            System.out.printf("Get repository information for %s%n", filePath);
            String storedData = Files.readString(filePath);
            List<CodeSearchResultItem> items = gson.fromJson(storedData, GithubCodeSearchResult.class).getItems();
            for (CodeSearchResultItem fileInfo : items) {
                try {
                    System.out.println("Pausing...");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Getting repository information for: " + fileInfo.getHtml_url());
                RepositoryInfo repositoryObject = fileInfo.getRepository();
                String repoUrl = repositoryObject.getUrl();
                String response = Main.gitHubRepositoryImplRaw.getContentOfUrl(repoUrl).getBody();
                RepositoryResult repoResult = gson.fromJson(response, RepositoryResult.class);

                repositoriesInformation.add(ProjectInfo.builder()
                        .apiUrl(repoUrl)
                        .stargazerCount(repoResult.getStargazers_count())
                        .subscribersCount(repoResult.getSubscribers_count())
                        .contributorsCount(getContributorsCount(repoResult.getContributors_url()))
                        .commitsCount(getCommitsCount(repoResult.getCommits_url(), repoResult.getDefault_branch()))
                        .archived(repoResult.getArchived())
                        .disabled(repoResult.getDisabled())
                        .language(repoResult.getLanguage())
                        .topics(repoResult.getTopics())
                        .description(repoResult.getDescription())
                        .createdAt(repoResult.getCreated_at())
                        .updatedAt(repoResult.getUpdated_at())
                        .pushedAt(repoResult.getPushed_at())
                        .serverlessFileInfo(ServerlessFileInfo.builder()
                                .name(fileInfo.getName())
                                .path(fileInfo.getPath())
                                .htmlUrl(fileInfo.getHtml_url())
                                .url(fileInfo.getUrl())
                                .downloadUrl(getDownloadUrl(fileInfo.getUrl()))
                                .build())
                        .build());
            }
        return repositoriesInformation;
    }

    private String getDownloadUrl(String url) throws IOException {
        HttpResponse contentOfUrl = Main.gitHubRepositoryImplRaw.getContentOfUrl(url);
        FileInfo fileInfo = gson.fromJson(contentOfUrl.getBody(), FileInfo.class);
        return fileInfo.getDownload_url();
    }

    Integer getCommitsCount(String commits_url, String default_branch) throws IOException {
        String url = commits_url.replace("{/sha}", "?sha=" + default_branch + "&per_page=1&page=1");
        // for debugging: System.out.println("Getting commit count from url: " + url);
        HttpResponse response = Main.gitHubRepositoryImplRaw.getContentOfUrl(url);
        return PaginationHelper.extractLastPageNumber(response);
    }

    private Integer getContributorsCount(String contributors_url) throws IOException {
        HttpResponse response = Main.gitHubRepositoryImplRaw.getContentOfUrl(contributors_url);
        JsonArray jsonElements = gson.fromJson(response.getBody(), JsonArray.class);
        return jsonElements.size();
    }


}
