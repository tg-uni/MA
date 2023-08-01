package de.uniba.pi.applicationsearcher.githubcollector.githubapi;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public
class GithubCodeSearchResult {
    List<CodeSearchResultItem> items;
    Integer total_count;
    Boolean incomplete_results;
}
