package com.jackyblackson.idunntemplates.backend.store.spec;

import com.jackyblackson.idunntemplates.backend.dto.VersionSearchCriteria;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TemplateVersionSpecifications {

    public static Specification<TemplateVersion> withCriteria(VersionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 关联 Template ID 筛选
            if (criteria.getTemplateId() != null) {
                // 因为 TemplateVersion 中有 template 字段 (@ManyToOne)
                // 我们可以直接通过 root.get("template").get("id") 访问，也可以用 Join
                Join<TemplateVersion, Template> templateJoin = root.join("template", JoinType.INNER);
                predicates.add(cb.equal(templateJoin.get("id"), criteria.getTemplateId()));
            }

            // 2. 提交者筛选
            if (criteria.getSubmitterId() != null) {
                predicates.add(cb.equal(root.get("submitterId"), criteria.getSubmitterId()));
            }

            // 3. 消息关键词 (模糊匹配)
            if (criteria.getMessageKeyword() != null && !criteria.getMessageKeyword().isEmpty()) {
                // result LIKE '%keyword%'
                predicates.add(cb.like(root.get("message"), "%" + criteria.getMessageKeyword() + "%"));
            }

            // 4. 版本号精确匹配
            if (criteria.getVersionId() != null && !criteria.getVersionId().isEmpty()) {
                predicates.add(cb.equal(root.get("versionId"), criteria.getVersionId()));
            }

            // 5. 时间范围筛选
            if (criteria.getMinCreatedAt() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getMinCreatedAt()));
            }
            if (criteria.getMaxCreatedAt() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getMaxCreatedAt()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}