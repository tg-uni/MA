package de.uniba.pi.applicationsearcher.githubcollector.githubapi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RepositoryResult {
    Integer stargazers_count;
    Integer subscribers_count;
    String contributors_url;
    String commits_url;
    String default_branch;
    String language;
    String description;
    List<String> topics;
    String updated_at;
    String pushed_at;
    String created_at;
    Boolean archived;
    Boolean disabled;
}
