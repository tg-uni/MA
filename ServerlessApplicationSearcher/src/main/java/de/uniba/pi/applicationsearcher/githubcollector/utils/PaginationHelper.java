package de.uniba.pi.applicationsearcher.githubcollector.utils;

import de.uniba.pi.applicationsearcher.model.HttpResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaginationHelper {
    public static Integer extractLastPageNumber(HttpResponse response) {
        Pattern pattern = Pattern.compile("<([^<]*)>; rel=\"last\"");
        final Matcher matcher = pattern.matcher(response.getHeaders().get("Link").get(0));
        String lastPageLink = null;
        while (matcher.find()) {
            lastPageLink = matcher.group(1);
        }
        if (lastPageLink != null) {
            final Matcher pageMatcher = Pattern.compile("[?|&]page=([0-9]*)(?:&|$)").matcher(lastPageLink);
            String pageNumber = null;
            while (pageMatcher.find()) {
                pageNumber = pageMatcher.group(1);
            }
            return Integer.parseInt(pageNumber);
        }
        return null;
    }

    public static String getNextPageLink(HttpResponse result) {
        Pattern pattern = Pattern.compile("<([^<]*)>; rel=\"next\"");
        final Matcher matcher = pattern.matcher(result.getHeaders().get("Link").get(0));
        String nextPageLink = null;
        while (matcher.find()) {
            nextPageLink = matcher.group(1);
        }
        if (nextPageLink != null) {
            nextPageLink = nextPageLink.replace("+", "%20");
        }
        return nextPageLink;
    }
}
