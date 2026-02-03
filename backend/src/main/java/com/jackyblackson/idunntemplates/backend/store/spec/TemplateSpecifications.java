package com.jackyblackson.idunntemplates.backend.store.spec;

import com.jackyblackson.idunntemplates.backend.dto.TemplateSearchCriteria;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TemplateSpecifications {

    public static Specification<Template> withCriteria(TemplateSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 关联 Metadata 表 (Left Join，防止 metadata 缺失导致查不到模板)
            Join<Template, TemplateMetadata> metadataJoin = root.join("metadata", JoinType.LEFT);

            // 2.1 路径前缀查询 (Like 'path%')
            if (criteria.getPathPrefix() != null && !criteria.getPathPrefix().isEmpty()) {
                predicates.add(cb.like(root.get("path"), criteria.getPathPrefix() + "%"));
            }

            // 2.2 路径模糊匹配 (Like 'path')
            if (criteria.getPathLike() != null && !criteria.getPathLike().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("path")),
                        "%" + criteria.getPathLike().toLowerCase() + "%"
                ));
            }

            // 2.3 路径模糊匹配 (Like 'name')
            if (criteria.getNameLike() != null && !criteria.getNameLike().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")),
                        "%" + criteria.getNameLike().toLowerCase() + "%"
                ));
            }

            // 3. Metadata 字段精确匹配
            if (criteria.getCreatorId() != null) {
                predicates.add(cb.equal(metadataJoin.get("creatorId"), criteria.getCreatorId()));
            }
            if (criteria.getWorldId() != null) {
                predicates.add(cb.equal(metadataJoin.get("worldId"), criteria.getWorldId()));
            }
            if (criteria.getLocked() != null) {
                predicates.add(cb.equal(metadataJoin.get("locked"), criteria.getLocked()));
            }

            // 4. 尺寸范围查询 (Range Query)
            if (criteria.getMinWidth() != null && criteria.getMinWidth() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(metadataJoin.get("width"), criteria.getMinWidth()));
            }
            if (criteria.getMaxWidth() != null && criteria.getMaxWidth() > 0) {
                predicates.add(cb.lessThanOrEqualTo(metadataJoin.get("width"), criteria.getMaxWidth()));
            }
            if (criteria.getMinHeight() != null && criteria.getMinHeight() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(metadataJoin.get("height"), criteria.getMinHeight()));
            }
            if (criteria.getMaxHeight() != null && criteria.getMaxHeight() > 0) {
                predicates.add(cb.lessThanOrEqualTo(metadataJoin.get("height"), criteria.getMaxHeight()));
            }
            if (criteria.getMinLength() != null && criteria.getMinLength() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(metadataJoin.get("length"), criteria.getMinLength()));
            }
            if (criteria.getMaxLength() != null && criteria.getMaxLength() > 0) {
                predicates.add(cb.lessThanOrEqualTo(metadataJoin.get("length"), criteria.getMaxLength()));
            }
            // ... 同理可以添加 Height 和 Length

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}