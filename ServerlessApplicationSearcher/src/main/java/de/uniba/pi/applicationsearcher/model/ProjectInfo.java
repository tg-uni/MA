package de.uniba.pi.applicationsearcher.model;

import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProjectInfo {
    String apiUrl;

    // replaces watchersCount, number of stars of the repository
    Integer stargazerCount;
    Integer subscribersCount;
    Integer contributorsCount;
    Integer commitsCount;

    String language;
    String description;
    List<String> topics;

    // changes to the repository, e.g. the description
    String updatedAt;
    // changes to one of the branches
    String pushedAt;
    String createdAt;

    Boolean archived;
    Boolean disabled;

    ServerlessFileInfo serverlessFileInfo;
}
