package de.uniba.pi.applicationsearcher.githubcollector.githubapi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public
class CodeSearchResultItem {
    RepositoryInfo repository;
    String html_url;
    String name;
    String path;
    String url;
}
