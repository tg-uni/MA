package de.uniba.pi.applicationsearcher.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class HttpResponse {
    String body;
    private Map<String, List<String>> headers;

}
