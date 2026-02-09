package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.domain.RemoteSet;
import com.jackyblackson.idunntemplates.backend.domain.RemoteSetSource;
import com.jackyblackson.idunntemplates.backend.dto.AddSourceRequest;
import com.jackyblackson.idunntemplates.backend.dto.CreateRemoteSetRequest;
import com.jackyblackson.idunntemplates.backend.dto.RemoteSetDto;
import com.jackyblackson.idunntemplates.backend.dto.RemoteSetSourceDto;
import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.backend.dto.TemplateWithColorsDto;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.backend.service.RemoteSetService;
import com.jackyblackson.idunntemplates.backend.service.TemplateColorService;
import com.jackyblackson.idunntemplates.backend.service.TemplateService;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateVersionRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/remote-sets")
public class RemoteSetController {

    private final RemoteSetService remoteSetService;
    private final LuckyPermAuthService luckyPermAuthService;
    private final TemplateColorService templateColorService;
    private final TemplateVersionRepository templateVersionRepository;
    private final TemplateService templateService;

    @Autowired
    public RemoteSetController(RemoteSetService remoteSetService,
                               LuckyPermAuthService luckyPermAuthService,
                               TemplateColorService templateColorService,
                               TemplateVersionRepository templateVersionRepository,
                               TemplateService templateService) {
        this.remoteSetService = remoteSetService;
        this.luckyPermAuthService = luckyPermAuthService;
        this.templateColorService = templateColorService;
        this.templateVersionRepository = templateVersionRepository;
        this.templateService = templateService;
    }

    @PostMapping
    @AuthRequired
    public ResponseEntity<RemoteSetDto> createSet(@RequestBody CreateRemoteSetRequest request, UserContext user) {
        String ns = request.getNamespace();
        boolean isSelf = ns.equals("player." + user.getUsername());
        if (!isSelf) {
            boolean isAdmin = luckyPermAuthService.checkPermission(user.getUuid().toString(), user.getUsername(), "idunn.admin");
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        RemoteSet set = remoteSetService.createSet(request.getName(), request.getNamespace(), UUID.fromString(user.getUuid()), user.getUsername());
        return ResponseEntity.ok(convertToDto(set, user, false));
    }

    @GetMapping("/{id}")
    @AuthRequired
    public ResponseEntity<RemoteSetDto> getSet(@PathVariable Long id, UserContext user) {
        return remoteSetService.getSetById(id)
                .map(set -> {
                    if (!canView(user, set.getNamespace().getName())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<RemoteSetDto>build();
                    }
                    return ResponseEntity.ok(convertToDto(set, user, true));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<RemoteSetDto>> searchSets(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String namespace,
            UserContext user,
            @PageableDefault(size = 20, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<RemoteSet> page = remoteSetService.searchSets(name, namespace, pageable);

        List<RemoteSet> content = page.getContent();
        List<RemoteSet> filtered = luckyPermAuthService.filterList(
                user.getUuid().toString(),
                user.getUsername(),
                content,
                set -> set.getNamespace().getName().equals("player." + user.getUsername()) ? null : "idunn.namespace.view." + set.getNamespace().getName()
        );

        List<RemoteSetDto> dtos = filtered.stream()
                .map(set -> convertToDto(set, user, true))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(dtos, page.getPageable(), page.getTotalElements()));
    }

    @PostMapping("/{id}/sources")
    @AuthRequired
    public ResponseEntity<RemoteSetSourceDto> addSource(@PathVariable Long id, @RequestBody AddSourceRequest request, UserContext user) {
        RemoteSet set = remoteSetService.getSetById(id).orElse(null);
        if (set == null) return ResponseEntity.notFound().build();
        if (!canEdit(user, set)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        RemoteSetSource source = remoteSetService.addSource(id, request.getType(), request.getPath(), request.getTargetSetId(), request.getWeight());
        return ResponseEntity.ok(convertToSourceDto(source));
    }

    @DeleteMapping("/sources/{sourceId}")
    @AuthRequired
    public ResponseEntity<Void> removeSource(@PathVariable Long sourceId, UserContext user) {
        RemoteSetSource source = remoteSetService.getSourceById(sourceId).orElse(null);
        if (source == null) return ResponseEntity.notFound().build();

        if (!canEdit(user, source.getRemoteSet())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        remoteSetService.removeSource(sourceId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/sources/{sourceId}")
    @AuthRequired
    public ResponseEntity<RemoteSetSourceDto> updateSourceWeight(@PathVariable Long sourceId, @RequestBody Map<String, Double> payload, UserContext user) {
        RemoteSetSource source = remoteSetService.getSourceById(sourceId).orElse(null);
        if (source == null) return ResponseEntity.notFound().build();

        if (!canEdit(user, source.getRemoteSet())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Double weight = payload.get("weight");
        if (weight == null) return ResponseEntity.badRequest().build();

        RemoteSetSource updated = remoteSetService.updateSourceWeight(sourceId, weight);
        return ResponseEntity.ok(convertToSourceDto(updated));
    }

    @GetMapping("/{id}/sources")
    @AuthRequired
    public ResponseEntity<List<RemoteSetSourceDto>> getSources(@PathVariable Long id, UserContext user) {
        RemoteSet set = remoteSetService.getSetById(id).orElse(null);
        if (set == null) return ResponseEntity.notFound().build();
        if (!canView(user, set.getNamespace().getName())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<RemoteSetSource> sources = remoteSetService.getSources(id);
        List<RemoteSetSourceDto> dtos = sources.stream().map(this::convertToSourceDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}/random")
    @AuthRequired
    public ResponseEntity<TemplateWithColorsDto> getRandomTemplate(@PathVariable Long id, UserContext user) {
        RemoteSet set = remoteSetService.getSetById(id).orElse(null);
        if (set == null) return ResponseEntity.notFound().build();
        if (!canView(user, set.getNamespace().getName())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return remoteSetService.getRandomTemplate(id)
                .map(this::convertToTemplateDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/dependencies")
    @AuthRequired
    public ResponseEntity<Map<String, List<RemoteSetDto>>> getDependencies(@PathVariable Long id, UserContext user) {
         RemoteSet set = remoteSetService.getSetById(id).orElse(null);
         if (set == null) return ResponseEntity.notFound().build();
         if (!canView(user, set.getNamespace().getName())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

         Map<String, List<RemoteSet>> dependencies = remoteSetService.getDependents(id);
         Map<String, List<RemoteSetDto>> result = new HashMap<>();

         result.put("direct", dependencies.getOrDefault("direct", Collections.emptyList()).stream()
                 .filter(s -> canView(user, s.getNamespace().getName()))
                 .map(s -> convertToDto(s, user, false))
                 .collect(Collectors.toList()));

         result.put("indirect", dependencies.getOrDefault("indirect", Collections.emptyList()).stream()
                 .filter(s -> canView(user, s.getNamespace().getName()))
                 .map(s -> convertToDto(s, user, false))
                 .collect(Collectors.toList()));

         return ResponseEntity.ok(result);
    }

    @GetMapping("/sources/{sourceId}/templates")
    @AuthRequired
    public ResponseEntity<Page<TemplateWithColorsDto>> getSourceTemplates(
            @PathVariable Long sourceId,
            UserContext user,
            @PageableDefault(size = 20, direction = Sort.Direction.ASC) Pageable pageable
    ) {
         RemoteSetSource source = remoteSetService.getSourceById(sourceId).orElse(null);
         if (source == null) return ResponseEntity.notFound().build();

         if (!canView(user, source.getRemoteSet().getNamespace().getName())) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
         }

         if (source.getType() == RemoteSetSource.SourceType.PATH) {
             TemplateSearchCriteria criteria = new TemplateSearchCriteria();
             criteria.setPathPrefix(source.getPath());

             Page<Template> page = templateService.searchTemplates(criteria, pageable, user);
             return ResponseEntity.ok(convertToTemplateDtoPage(page, user));
         } else {
             return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
         }
    }

    private boolean canView(UserContext user, String namespace) {
        if (namespace.equals("player." + user.getUsername())) return true;
        return luckyPermAuthService.checkPermission(user.getUuid().toString(), user.getUsername(), "idunn.namespace.view." + namespace);
    }

    private boolean canEdit(UserContext user, RemoteSet set) {
        return set.getCreatorUuid().toString().equals(user.getUuid()) || luckyPermAuthService.checkPermission(user.getUuid(), user.getUsername(), "idunn.admin");
    }

    private RemoteSetSourceDto convertToSourceDto(RemoteSetSource source) {
        RemoteSetSourceDto dto = new RemoteSetSourceDto();
        dto.setId(source.getId());
        dto.setWeight(source.getWeight());
        dto.setType(source.getType());
        dto.setPath(source.getPath());
        if (source.getTargetSet() != null) {
            dto.setTargetSetId(source.getTargetSet().getId());
            dto.setTargetSetName(source.getTargetSet().getName());
        }
        return dto;
    }

    private TemplateWithColorsDto convertToTemplateDto(Template t) {
        List<Template> list = Collections.singletonList(t);
        Map<UUID, List<String>> colors = templateColorService.resolveColorsForTemplates(list);
        TemplateWithColorsDto dto = new TemplateWithColorsDto(t, colors.getOrDefault(t.getId(), Collections.emptyList()));
        // Version info?
        List<TemplateVersion> versions = templateVersionRepository.findByTemplateIn(list);
        if (!versions.isEmpty()) {
            dto.setLatestVersionName(versions.get(0).getVersionId());
            dto.setVersionCount(versions.size());
        }
        return dto;
    }

    private Page<TemplateWithColorsDto> convertToTemplateDtoPage(Page<Template> page, UserContext userContext) {
        List<Template> originalContent = page.getContent();
        List<Template> nonNull = originalContent.stream().filter(Objects::nonNull).collect(Collectors.toList());
        Map<UUID, List<String>> colors = templateColorService.resolveColorsForTemplates(nonNull);

        List<String> distinctPaths = nonNull.stream()
                .map(Template::getPath)
                .filter(Objects::nonNull)
                .map(path -> PermissionNames.Templates.commitToPath$R + "." + path.replace("/", "."))
                .distinct()
                .collect(Collectors.toList());

        Map<String, Boolean> commitPermResults = (userContext != null && !distinctPaths.isEmpty()) ?
                luckyPermAuthService.batchCheckPermissions(userContext.getUuid(), userContext.getUsername(), distinctPaths) :
                Collections.emptyMap();

        Map<UUID, List<TemplateVersion>> versionsMap = nonNull.isEmpty() ? Collections.emptyMap() :
                templateVersionRepository.findByTemplateIn(nonNull).stream()
                        .filter(v -> v != null && v.getTemplate() != null)
                        .collect(Collectors.groupingBy(v -> v.getTemplate().getId()));

        List<TemplateWithColorsDto> dtos = originalContent.stream().map(t -> {
            if (t == null) return null;
            TemplateWithColorsDto dto = new TemplateWithColorsDto(t, colors.getOrDefault(t.getId(), Collections.emptyList()));

            List<TemplateVersion> allVersions = versionsMap.getOrDefault(t.getId(), Collections.emptyList());
            List<TemplateVersion> sortedVersions = allVersions.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingLong((TemplateVersion v) ->
                            Optional.ofNullable(v.getCreatedAt()).orElse(0L)).reversed())
                    .toList();

            dto.setVersionCount(sortedVersions.size());
            if (!sortedVersions.isEmpty()) {
                dto.setLatestVersionName(sortedVersions.get(0).getVersionId());
                dto.setLatestVersions(sortedVersions.stream().limit(10).collect(Collectors.toList()));
            } else {
                dto.setLatestVersions(Collections.emptyList());
            }

            dto.setCanUse(true);

            String path = t.getPath();
            if (path != null) {
                String commitPerm = PermissionNames.Templates.commitToPath$R + "." + path.replace("/", ".");
                dto.setCanCommit(commitPermResults.getOrDefault(commitPerm, false));
            } else {
                dto.setCanCommit(false);
            }
            return dto;
        }).collect(Collectors.toList());

        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    private RemoteSetDto convertToDto(RemoteSet set, UserContext user, boolean includePreview) {
        List<TemplateWithColorsDto> previews = Collections.emptyList();
        if (includePreview) {
            List<Template> templates = remoteSetService.getPreviewTemplates(set.getId());
            if (!templates.isEmpty()) {
                Map<UUID, List<String>> colors = templateColorService.resolveColorsForTemplates(templates);
                Map<UUID, List<TemplateVersion>> versionsMap = templateVersionRepository.findByTemplateIn(templates).stream()
                        .filter(v -> v != null && v.getTemplate() != null)
                        .collect(Collectors.groupingBy(v -> v.getTemplate().getId()));

                previews = templates.stream().map(t -> {
                     TemplateWithColorsDto dto = new TemplateWithColorsDto(t, colors.getOrDefault(t.getId(), Collections.emptyList()));
                     List<TemplateVersion> vs = versionsMap.getOrDefault(t.getId(), Collections.emptyList());
                     vs.sort(Comparator.comparingLong((TemplateVersion v) -> Optional.ofNullable(v.getCreatedAt()).orElse(0L)).reversed());
                     dto.setVersionCount(vs.size());
                     if (!vs.isEmpty()) {
                         dto.setLatestVersionName(vs.get(0).getVersionId());
                     }
                     return dto;
                }).collect(Collectors.toList());
            }
        }

        return new RemoteSetDto(
                set.getId(),
                set.getName(),
                set.getCreatorUuid(),
                set.getNamespace().getName(),
                previews
        );
    }
}
