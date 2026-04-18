package com.jackyblackson.idunntemplates.core.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jackyblackson.idunntemplates.IdunnTemplates;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class BackendApiClient {

    private final String baseUrl;
    private final String serverToken;
    private final HttpClient httpClient;
    private final Gson gson;

    public BackendApiClient(String baseUrl, String serverToken) {
        if (baseUrl != null && baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = baseUrl;
        }
        this.serverToken = serverToken;
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // Reasonable timeout for game server
                .build();
        this.gson = new Gson();
    }

    /**
     * Asynchronously fetches a random template UUID from the given collection ID.
     * Retries or fails gracefully.
     *
     * @param collectionId the ID of the collection
     * @return CompletableFuture resolving to the UUID of the template, or null if an error occurs.
     */
    public CompletableFuture<UUID> getRandomTemplateFromCollection(long collectionId) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            IdunnTemplates.getInstance().getLogger().warning("Backend API URL is not configured (services.backend.api-url)!");
            return CompletableFuture.completedFuture(null);
        }

        // e.g., /api/v1/collections/1/random
        String urlString = baseUrl + "/collections/" + collectionId + "/random";
        
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(3))
                    .GET();

            if (serverToken != null && !serverToken.isEmpty()) {
                builder.header("Authorization", serverToken);
            }

            HttpRequest request = builder.build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                            if (json != null && json.has("id")) {
                                String idString = json.get("id").getAsString();
                                return UUID.fromString(idString);
                            }
                        } else {
                            IdunnTemplates.getInstance().getLogger().warning("Failed to fetch random template for collection " + collectionId + ". Status: " + response.statusCode());
                        }
                        return null;
                    })
                    .exceptionally(ex -> {
                        IdunnTemplates.getInstance().getLogger().warning("HTTP request failed: " + ex.getMessage());
                        return null;
                    });

        } catch (IllegalArgumentException e) {
            IdunnTemplates.getInstance().getLogger().warning("Invalid URL format for collection random fetch. " + e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
    }

    public CompletableFuture<ProjectCreateResponse> createInGameProject(ProjectCreateRequest payload) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            IdunnTemplates.getInstance().getLogger().warning("Backend API URL is not configured (services.backend.api-url)!");
            return CompletableFuture.completedFuture(new ProjectCreateResponse(false, "Backend API URL is not configured", null, null));
        }

        String urlString = baseUrl + "/commercial/projects/in-game";

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)));

            if (serverToken != null && !serverToken.isEmpty()) {
                builder.header("Authorization", serverToken);
            }

            HttpRequest request = builder.build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            ProjectSummary summary = gson.fromJson(response.body(), ProjectSummary.class);
                            return new ProjectCreateResponse(true, null, summary, response.body());
                        }
                        IdunnTemplates.getInstance().getLogger().warning(
                                "Failed to create in-game project. Status: " + response.statusCode() + ", body: " + response.body()
                        );
                        return new ProjectCreateResponse(false, response.body(), null, response.body());
                    })
                    .exceptionally(ex -> {
                        IdunnTemplates.getInstance().getLogger().warning("Create in-game project request failed: " + ex.getMessage());
                        return new ProjectCreateResponse(false, ex.getMessage(), null, null);
                    });
        } catch (IllegalArgumentException e) {
            IdunnTemplates.getInstance().getLogger().warning("Invalid URL format for in-game project creation. " + e.getMessage());
            return CompletableFuture.completedFuture(new ProjectCreateResponse(false, e.getMessage(), null, null));
        }
    }

    public static class ProjectCreateRequest {
        public String name;
        public String displayName;
        public String description;
        public String pathName;
        public String kind;
        public String modelKind;
        public String worldName;
        public Integer minX;
        public Integer minY;
        public Integer minZ;
        public Integer maxX;
        public Integer maxY;
        public Integer maxZ;
        public Double tpX;
        public Double tpY;
        public Double tpZ;
        public Double tpYaw;
        public Double tpPitch;
        public Long parentProjectId;
        public String creatorUsername;
        public String sourceServerName;
    }

    public static class ProjectSummary {
        public Long id;
        public String name;
        public String displayName;
        public String pathName;
        public String kind;
        public Long worldId;
    }

    public static class ProjectDetails {
        public Long id;
        public String name;
        public String displayName;
        public String description;
        public String pathName;
        public String kind;
        public String modelKind;
        public Long worldId;
        public String worldName;
        public String worldMountName;
        public Integer minX;
        public Integer minY;
        public Integer minZ;
        public Integer maxX;
        public Integer maxY;
        public Integer maxZ;
        public Long createTimeMs;
        public Long deleteTimeMs;
    }

    public static class ProjectPageResponse {
        public List<ProjectDetails> content = List.of();
        public int totalPages;
        public int number;
    }

    public static class ProjectSettlementSnapshotRequest {
        public Long projectEffectiveBlocks;
        public Long scannedAtMs;
        public String sourceServerName;
        public List<ProjectTemplateUsageSnapshot> templates;
        public List<ProjectInstanceUsageSnapshot> instances;
    }

    public static class ProjectTemplateUsageSnapshot {
        public String templateId;
        public String templateName;
        public Integer usageCount = 0;
        public Long totalManagedBlocks = 0L;
        public List<ProjectTemplateVersionUsageSnapshot> versions = new java.util.ArrayList<>();
    }

    public static class ProjectTemplateVersionUsageSnapshot {
        public String versionId;
        public Integer usageCount = 0;
        public Long totalManagedBlocks = 0L;
    }

    public static class ProjectInstanceUsageSnapshot {
        public String instanceId;
        public String templateId;
        public String templateName;
        public String versionId;
        public String placedByName;
        public Long placedAt;
        public Integer minX;
        public Integer minY;
        public Integer minZ;
        public Integer maxX;
        public Integer maxY;
        public Integer maxZ;
        public Long managedBlocks = 0L;
    }

    public static class ProjectCreateResponse {
        private final boolean success;
        private final String errorMessage;
        private final ProjectSummary project;
        private final String rawBody;

        public ProjectCreateResponse(boolean success, String errorMessage, ProjectSummary project, String rawBody) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.project = project;
            this.rawBody = rawBody;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public ProjectSummary getProject() {
            return project;
        }

        public String getRawBody() {
            return rawBody;
        }
    }

    public CompletableFuture<ProjectDetails> getProject(long projectId) {
        return sendAuthorizedGet(baseUrl + "/commercial/projects/" + projectId, ProjectDetails.class);
    }

    public CompletableFuture<List<ProjectDetails>> listAllProjects() {
        return getProjectsPage(0, 200).thenCompose(firstPage -> {
            if (firstPage == null) {
                return CompletableFuture.completedFuture(List.of());
            }
            if (firstPage.totalPages <= 1) {
                return CompletableFuture.completedFuture(firstPage.content == null ? List.of() : firstPage.content);
            }

            List<CompletableFuture<ProjectPageResponse>> pageFutures = IntStream.range(1, firstPage.totalPages)
                    .mapToObj(page -> getProjectsPage(page, 200))
                    .toList();

            return CompletableFuture.allOf(pageFutures.toArray(CompletableFuture[]::new))
                    .thenApply(ignored -> {
                        List<ProjectDetails> projects = new ArrayList<>();
                        if (firstPage.content != null) {
                            projects.addAll(firstPage.content);
                        }
                        for (CompletableFuture<ProjectPageResponse> future : pageFutures) {
                            ProjectPageResponse page = future.join();
                            if (page != null && page.content != null) {
                                projects.addAll(page.content);
                            }
                        }
                        return projects;
                    });
        });
    }

    public CompletableFuture<List<ProjectDetails>> findOverlappingProjects(
            String worldName,
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        String urlString = String.format(
                "%s/commercial/projects/overlap?worldName=%s&minX=%d&minY=%d&minZ=%d&maxX=%d&maxY=%d&maxZ=%d",
                baseUrl,
                encode(worldName),
                minX, minY, minZ, maxX, maxY, maxZ
        );
        return sendAuthorizedGet(urlString, ProjectDetails[].class)
                .thenApply(response -> response == null ? List.of() : Arrays.asList(response));
    }

    public CompletableFuture<Boolean> uploadProjectSettlementSnapshot(long projectId, ProjectSettlementSnapshotRequest payload) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        String urlString = baseUrl + "/commercial/projects/" + projectId + "/settlement-snapshot";
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)));

            if (serverToken != null && !serverToken.isEmpty()) {
                builder.header("Authorization", serverToken);
            }

            return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() >= 200 && response.statusCode() < 300)
                    .exceptionally(ex -> {
                        IdunnTemplates.getInstance().getLogger().warning("Settlement snapshot upload failed: " + ex.getMessage());
                        return false;
                    });
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(false);
        }
    }

    private <T> CompletableFuture<T> sendAuthorizedGet(String urlString, Class<T> responseType) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .timeout(Duration.ofSeconds(5))
                    .GET();

            if (serverToken != null && !serverToken.isEmpty()) {
                builder.header("Authorization", serverToken);
            }

            return httpClient.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            return gson.fromJson(response.body(), responseType);
                        }
                        IdunnTemplates.getInstance().getLogger().warning("Backend GET failed: " + urlString + ", status=" + response.statusCode());
                        return null;
                    })
                    .exceptionally(ex -> {
                        IdunnTemplates.getInstance().getLogger().warning("Backend GET request failed: " + ex.getMessage());
                        return null;
                    });
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<ProjectPageResponse> getProjectsPage(int page, int size) {
        String urlString = String.format(
                "%s/commercial/projects?page=%d&size=%d&sort=id,desc",
                baseUrl,
                page,
                size
        );
        return sendAuthorizedGet(urlString, ProjectPageResponse.class);
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
