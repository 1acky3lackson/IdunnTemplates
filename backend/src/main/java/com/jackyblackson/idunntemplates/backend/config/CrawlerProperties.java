package com.jackyblackson.idunntemplates.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.crawler")
public class CrawlerProperties {

    private long intervalMs;
    private Ne ne;

    @Data
    public static class Ne {
        private long compProductSpan;
        private long peProductSpan;
        private long peProductOrderDays;
        private long peProductStatDays;
        private long peProductCommentSpan;
        private long peProductFeedbackSpan;
        private long intervalMs;
        private Map<String, NeUser> users;
    }

    @Data
    public static class NeUser {
        private String name;
        private Map<String, String> headers;
    }
}
