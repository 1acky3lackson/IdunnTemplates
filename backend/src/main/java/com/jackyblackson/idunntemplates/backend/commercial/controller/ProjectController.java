package com.jackyblackson.idunntemplates.backend.commercial.controller;

import com.jackyblackson.idunntemplates.backend.annotation.AuthRequired;
import com.jackyblackson.idunntemplates.backend.commercial.entity.Project;
import com.jackyblackson.idunntemplates.backend.commercial.entity.World;
import com.jackyblackson.idunntemplates.backend.commercial.repository.ProjectRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.WorldRepository;
import com.jackyblackson.idunntemplates.backend.commercial.repository.chekout.UserProjectContributionRepository;
import com.jackyblackson.idunntemplates.backend.commercial.service.UserProjectContributionService;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.permission.PermissionNames;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * RESTful API for managing Project entities.
 */
@RestController
@RequestMapping("/api/v1/commercial/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final WorldRepository worldRepository;
    private final LuckyPermAuthService authService;
    private final UserProjectContributionService userProjectContributionService;
    private final UserProjectContributionRepository userProjectContributionRepository;

    // ---------- 查询接口 ----------

    /**
     * 查询项目列表，支持动态搜索和分页。
     *
     * @param search   查询条件字符串，格式：字段:值 或 字段~:值（模糊查询），多个条件用逗号分隔。
     *                 例如：name~:测试,world.id:1,kind:BUILD
     * @param pageable 分页参数，默认页码0，每页20条，按id降序排序。
     * @return 分页的项目DTO
     */
    @GetMapping
    @AuthRequired
    public ResponseEntity<Page<ProjectDto>> listProjects(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            UserContext user
    ) {
        boolean canListAll = authService.checkPermission(user, PermissionNames.Commercial.Project.listAll);

        Specification<Project> spec = buildSpecification(search);
        Page<Project> page = null;
        if (canListAll) {
            page = projectRepository.findAll(spec, pageable);
        } else {
            page = userProjectContributionService.getUserParticipatedProjects(user.getUsername(), spec, pageable);
        }
        Page<ProjectDto> dtoPage = page.map(this::convertToDto);

        return ResponseEntity.ok(dtoPage);
    }

    /**
     * 根据ID查询单个项目详情。
     *
     * @param id 项目ID
     * @return 项目DTO，若不存在返回404
     */
    @GetMapping("/{id}")
    @AuthRequired
    @Transactional
    public ResponseEntity<ProjectDto> getProject(@PathVariable Long id, UserContext user) {
        boolean isContributor = userProjectContributionService.isUserParticipant(user.getUsername(), id);
        boolean checkAll = authService.checkPermission(user, PermissionNames.Commercial.Project.listAll);
        if (!isContributor && !checkAll) {
            return ResponseEntity.status(406).build();
        }
        return projectRepository.findById(id)
                .map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------- 创建接口 ----------

    /**
     * 创建新项目。
     *
     * @param request 创建请求体（包含必要的项目信息）
     * @return 创建后的项目DTO，状态码201
     */
    @PostMapping
    @Transactional
    @AuthRequired
    public ResponseEntity<ProjectDto> createProject(@RequestBody ProjectCreateRequest request, UserContext user) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.Project.create)) {
            return ResponseEntity.status(406).build();
        }
        Project project = new Project();
        updateProjectFromRequest(project, request);

        // 自动设置创建时间（毫秒）
        project.setCreateTimeMs(System.currentTimeMillis());

        Project saved = projectRepository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(saved));
    }

    // ---------- 更新接口 ----------

    /**
     * 全量更新项目信息。
     *
     * @param id      项目ID
     * @param request 更新请求体
     * @return 更新后的项目DTO
     */
    @PutMapping("/{id}")
    @Transactional
    @AuthRequired
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id,
                                                    @RequestBody ProjectCreateRequest request,
                                                    UserContext user
    ) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.Project.modify)) {
            return ResponseEntity.status(406).build();
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        updateProjectFromRequest(project, request);
        // 注意：创建时间不应被更新，deleteTime也不在此修改

        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(convertToDto(saved));
    }

    // ---------- 删除接口（软删除） ----------

    /**
     * 软删除项目（设置 deleteTimeMs 为当前时间戳）。
     *
     * @param id 项目ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Transactional
    @AuthRequired
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, UserContext user) {
        if (!authService.checkPermission(user, PermissionNames.Commercial.Project.delete)) {
            return ResponseEntity.status(406).build();
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        project.setDeleteTimeMs(System.currentTimeMillis());
        projectRepository.save(project);
        return ResponseEntity.noContent().build();
    }

    // ---------- 辅助方法 ----------

    /**
     * 将 Project 实体转换为 ProjectDto。
     */
    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDisplayName(project.getDisplayName());
        dto.setDescription(project.getDescription());
        dto.setPathName(project.getPathName());
        dto.setKind(project.getKind());
        dto.setModelKind(project.getModelKind());
        dto.setMinX(project.getMinX());
        dto.setMinY(project.getMinY());
        dto.setMinZ(project.getMinZ());
        dto.setMaxX(project.getMaxX());
        dto.setMaxY(project.getMaxY());
        dto.setMaxZ(project.getMaxZ());
        dto.setTpX(project.getTpX());
        dto.setTpY(project.getTpY());
        dto.setTpZ(project.getTpZ());
        dto.setTpYaw(project.getTpYaw());
        dto.setTpPitch(project.getTpPitch());
        dto.setCreateTimeMs(project.getCreateTimeMs());
        dto.setDeleteTimeMs(project.getDeleteTimeMs());

        // 关联对象只保留ID
        if (project.getWorld() != null) {
            dto.setWorldId(project.getWorld().getId());
        }
        if (project.getParentProject() != null) {
            dto.setParentProjectId(project.getParentProject().getId());
        }
        // 不返回 childProjects 列表，避免数据量过大
        return dto;
    }

    /**
     * 从请求对象更新实体字段（共用创建和更新）。
     */
    private void updateProjectFromRequest(Project project, ProjectCreateRequest request) {
        project.setName(request.getName());
        project.setDisplayName(request.getDisplayName());
        project.setDescription(request.getDescription());
        project.setPathName(request.getPathName());
        project.setKind(request.getKind());
        project.setModelKind(request.getModelKind());
        project.setMinX(request.getMinX());
        project.setMinY(request.getMinY());
        project.setMinZ(request.getMinZ());
        project.setMaxX(request.getMaxX());
        project.setMaxY(request.getMaxY());
        project.setMaxZ(request.getMaxZ());
        project.setTpX(request.getTpX());
        project.setTpY(request.getTpY());
        project.setTpZ(request.getTpZ());
        project.setTpYaw(request.getTpYaw());
        project.setTpPitch(request.getTpPitch());

        // 处理关联的 World
        if (request.getWorldId() != null) {
            World world = worldRepository.findById(request.getWorldId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "World not found with id: " + request.getWorldId()));
            project.setWorld(world);
        } else {
            project.setWorld(null);
        }

        // 处理父项目（不能将自己设为父项目）
        if (request.getParentProjectId() != null) {
            if (project.getId() != null && project.getId().equals(request.getParentProjectId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent project cannot be itself");
            }
            Project parent = projectRepository.findById(request.getParentProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Parent project not found with id: " + request.getParentProjectId()));
            project.setParentProject(parent);
        } else {
            project.setParentProject(null);
        }
    }

    /**
     * 构建动态查询 Specification。
     */
    private Specification<Project> buildSpecification(String search) {
        if (search == null || search.trim().isEmpty()) {
            return Specification.where(null);
        }

        List<SearchCriteria> criteriaList = parseSearch(search);

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (SearchCriteria criteria : criteriaList) {
                Path<?> path = resolvePath(root, criteria.getField());
                if (path == null) continue; // 无法解析的字段忽略
                Object value = convertValue(path.getJavaType(), criteria.getValue());

                switch (criteria.getOperator()) {
                    case EQ:
                        predicates.add(cb.equal(path, value));
                        break;
                    case LIKE:
                        // 仅对字符串类型执行模糊查询
                        if (path.getJavaType() == String.class) {
                            predicates.add(cb.like((Path<String>) path, "%" + value + "%"));
                        }
                        break;
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 解析字段路径（支持点号嵌套）。
     */
    private Path<?> resolvePath(Path<?> root, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    /**
     * 解析 search 字符串为条件列表。
     */
    private List<SearchCriteria> parseSearch(String search) {
        List<SearchCriteria> list = new ArrayList<>();
        String[] conditions = search.split(",");
        for (String condition : conditions) {
            String[] parts = condition.split(":", 2);
            if (parts.length != 2) continue;

            String fieldWithOp = parts[0].trim();
            String value = parts[1].trim();

            Operator op = Operator.EQ;
            String field = fieldWithOp;
            if (fieldWithOp.endsWith("~")) {
                op = Operator.LIKE;
                field = fieldWithOp.substring(0, fieldWithOp.length() - 1).trim();
            }

            if (!field.isEmpty() && !value.isEmpty()) {
                list.add(new SearchCriteria(field, op, value));
            }
        }
        return list;
    }

    /**
     * 将字符串值转换为字段对应的 Java 类型。
     */
    private Object convertValue(Class<?> targetType, String value) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == Long.class || targetType == long.class) {
            return Long.parseLong(value);
        } else if (targetType == Integer.class || targetType == int.class) {
            return Integer.parseInt(value);
        } else if (targetType == Double.class || targetType == double.class) {
            return Double.parseDouble(value);
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            // 对于关联ID，通常为Long，但路径如 world.id 最终类型为 Long，已经覆盖
            return value;
        }
    }

    // ---------- 内部类（DTO 和请求对象） ----------

    /**
     * 项目DTO（响应结构）
     */
    @Getter
    @Setter
    public static class ProjectDto {
        private Long id;
        private String name;
        private String displayName;
        private String description;
        private String pathName;
        private String kind;
        private String modelKind;
        private Long worldId;
        private Integer minX;
        private Integer minY;
        private Integer minZ;
        private Integer maxX;
        private Integer maxY;
        private Integer maxZ;
        private Double tpX;
        private Double tpY;
        private Double tpZ;
        private Double tpYaw;
        private Double tpPitch;
        private Long parentProjectId;
        private Long createTimeMs;
        private Long deleteTimeMs;
        // 可选的格式化时间字段，由前端决定是否使用
    }

    /**
     * 项目创建/更新请求体（与DTO类似，但省略只读字段）
     */
    @Getter
    @Setter
    public static class ProjectCreateRequest {
        private String name;
        private String displayName;
        private String description;
        private String pathName;
        private String kind;
        private String modelKind;
        private Long worldId;          // 关联世界的ID
        private Integer minX;
        private Integer minY;
        private Integer minZ;
        private Integer maxX;
        private Integer maxY;
        private Integer maxZ;
        private Double tpX;
        private Double tpY;
        private Double tpZ;
        private Double tpYaw;
        private Double tpPitch;
        private Long parentProjectId;   // 父项目ID，可选
    }

    /**
     * 内部类：查询条件
     */
    @Getter
    private static class SearchCriteria {
        private final String field;
        private final Operator operator;
        private final String value;

        public SearchCriteria(String field, Operator operator, String value) {
            this.field = field;
            this.operator = operator;
            this.value = value;
        }
    }

    /**
     * 操作符枚举
     */
    private enum Operator {
        EQ,      // 等于
        LIKE     // 模糊查询
    }
}
