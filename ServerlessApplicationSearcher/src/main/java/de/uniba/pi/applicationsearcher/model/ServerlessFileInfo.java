package de.uniba.pi.applicationsearcher.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerlessFileInfo {
    String htmlUrl;
    String name;
    String path;
    String runtime;
    String serverlessVersion;
    // url for the GitHub file resource containing the download url
    String url;
    String downloadUrl;
    List<String> plugins;
}
