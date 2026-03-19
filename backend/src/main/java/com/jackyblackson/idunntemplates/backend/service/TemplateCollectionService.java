package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.domain.TemplateCollection;
import com.jackyblackson.idunntemplates.backend.domain.TemplateCollectionRelation;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateCollectionRelationRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateCollectionRepository;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@AllArgsConstructor
public class TemplateCollectionService {

    private final TemplateCollectionRepository collectionRepository;
    private final TemplateCollectionRelationRepository relationRepository;
    private final TemplateRepository templateRepository;

    @Transactional
    public TemplateCollection createCollection(String name, String description, boolean isPrivate, UserContext user) {
        TemplateCollection collection = new TemplateCollection();
        collection.setName(name);
        collection.setDescription(description);
        collection.setPrivateCollection(isPrivate);
        collection.setCreatorName(user.getUsername()); // 假设 UserContext 中有 getUsername()
        long now = System.currentTimeMillis();
        collection.setCreateTimeMs(now);
        collection.setUpdateTimeMs(now);

        return collectionRepository.save(collection);
    }

    @Transactional
    public TemplateCollection updateCollection(Long id, String name, String description, Boolean isPrivate, UserContext user, boolean viewAll) {
        TemplateCollection collection = getCollectionAndCheckPermission(id, user, viewAll);

        if (name != null) collection.setName(name);
        if (description != null) collection.setDescription(description);
        if (isPrivate != null) collection.setPrivateCollection(isPrivate);

        collection.setUpdateTimeMs(System.currentTimeMillis());
        return collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(Long id, UserContext user, boolean viewAll) {
        TemplateCollection collection = getCollectionAndCheckPermission(id, user, viewAll);
        // 先删除关联关系
        relationRepository.deleteByCollectionId(collection.getId());
        // 再删除合集
        collectionRepository.delete(collection);
    }

    @Transactional
    public void addTemplateToCollection(Long collectionId, UUID templateId, UserContext user, boolean viewAll) {
        TemplateCollection collection = getCollectionAndCheckPermission(collectionId, user, viewAll);

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found"));

        if (relationRepository.existsByCollectionIdAndTemplateId(collectionId, templateId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Template already exists in this collection");
        }

        TemplateCollectionRelation relation = new TemplateCollectionRelation();
        relation.setCollection(collection);
        relation.setTemplate(template);
        relation.setModifyTimeMs(System.currentTimeMillis());
        relationRepository.save(relation);
    }

    @Transactional
    public void removeTemplateFromCollection(Long collectionId, UUID templateId, UserContext user, boolean viewAll) {
        TemplateCollection collection = getCollectionAndCheckPermission(collectionId, user, viewAll);

        TemplateCollectionRelation relation = relationRepository.findByCollectionIdAndTemplateId(collectionId, templateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Relation not found"));

        relationRepository.delete(relation);
    }

    /**
     * 获取合集并检查用户是否有权限修改/删除
     * (如果是自己创建的，或者拥有 viewAll 管理员权限，则放行)
     */
    private TemplateCollection getCollectionAndCheckPermission(Long id, UserContext user, boolean viewAll) {
        TemplateCollection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found"));

        if (!viewAll && !collection.getCreatorName().equals(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to modify this collection");
        }
        return collection;
    }

    /**
     * 获取合集下的 Template 列表，返回构造好的子查询 Specification
     */
    public Specification<Template> getTemplatesInCollectionSpec(Long collectionId, UserContext user, boolean viewAll) {
        // 1. 鉴权：确保用户有权限查看此合集
        // 如果合集是私有的且不是自己创建的，且没有 viewAll 权限，会在这里抛出 403
        getCollectionAndCheckPermission(collectionId, user, viewAll);

        // 2. 构造子查询：查找所有属于该 collectionId 的 Template ID
        return (root, query, criteriaBuilder) -> {
            // 构造子查询 select template_id from idunn_template_collection_relation where collection_id = ?
            jakarta.persistence.criteria.Subquery<UUID> subquery = query.subquery(UUID.class);
            jakarta.persistence.criteria.Root<TemplateCollectionRelation> relationRoot = subquery.from(TemplateCollectionRelation.class);

            subquery.select(relationRoot.get("template").get("id"));
            subquery.where(criteriaBuilder.equal(relationRoot.get("collection").get("id"), collectionId));

            // 主查询：where template.id in (subquery)
            return criteriaBuilder.in(root.get("id")).value(subquery);
        };
    }
}
