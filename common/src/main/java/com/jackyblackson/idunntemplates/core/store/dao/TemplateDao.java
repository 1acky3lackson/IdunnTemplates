package com.jackyblackson.idunntemplates.core.store.dao;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.jackyblackson.idunntemplates.core.domain.Template;
import com.jackyblackson.idunntemplates.core.domain.TemplateMetadata;
import com.jackyblackson.idunntemplates.core.domain.TemplateVersion;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TemplateDao extends BaseDaoImpl<Template, UUID> {

    // 需要访问 InstanceDao 来查询关联
    private Dao<Instance, String> instanceDao;

    // 还需要访问 MetadataDao (通常由 ORMLite 内部管理，但为了显式操作也可以持有)
    private Dao<TemplateMetadata, UUID> metadataDao;
    private Dao<TemplateVersion, Integer> versionDao;

    public TemplateDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Template.class);
        // 初始化其他 DAO
        this.instanceDao = DaoManager.createDao(connectionSource, Instance.class);
        this.metadataDao = DaoManager.createDao(connectionSource, TemplateMetadata.class);
        // [新增] 初始化 Version DAO
        this.versionDao = DaoManager.createDao(connectionSource, TemplateVersion.class);
    }

    @Override
    public int create(Template template) throws SQLException {
        // 1. 确保 JSON 数据已同步
        template.getMetadata().prePersist();

        // 2. 显式保存 Metadata (虽然 foreignAutoCreate=true 可能处理，但显式更安全)
        metadataDao.createOrUpdate(template.getMetadata());

        // 3. 保存 Template
        return super.create(template);
    }

    @Override
    public int update(Template template) throws SQLException {
        template.getMetadata().prePersist();
        metadataDao.update(template.getMetadata());
        return super.update(template);
    }

    /**
     * 加载模板并 "Hydrate" (填充) 它的复杂关联数据 (Instance Maps)
     */
    public Template queryForIdWithRelations(UUID id) throws SQLException {
        Template template = queryForId(id);
        if (template != null) {
            hydrateTemplate(template);
        }
        return template;
    }

    public Template queryByPath(String path) throws SQLException {
        Template template = queryBuilder().where().eq("path", path).queryForFirst();
        if (template != null) {
            hydrateTemplate(template);
        }
        return template;
    }

    public List<Template> queryAllWithRelations() throws SQLException {
        List<Template> templates = queryForAll();
        for (Template t : templates) {
            hydrateTemplate(t);
        }
        return templates;
    }

    private void hydrateTemplate(Template template) throws SQLException {
        TemplateMetadata meta = template.getMetadata();
        if (meta == null) return;

        meta.postLoad(); // 解析 StagedChanges

        // 1. [新增] 填充 Versions
        // SQL: SELECT * FROM idunn_template_versions WHERE template_id = ? ORDER BY created_at ASC
        List<TemplateVersion> versions = versionDao.queryBuilder()
                .orderBy("created_at", true)
                .where()
                .eq("template_id", template.getId())
                .query();
        meta.hydrateVersions(versions);

        // 2. 填充 Child Instances
        List<Instance> childInstances = instanceDao.queryBuilder()
                .where()
                .eq("embedded_in_template_id", template.getId())
                .and().isNull("deleted_timestamp")
                .query();
        meta.hydrateChildInstances(childInstances);

        // 3. 填充 Parent Instances
        List<Instance> parentInstances = instanceDao.queryBuilder()
                .where()
                .eq("template_id", template.getId())
                .and().isNotNull("embedded_in_template_id")
                .and().isNull("deleted_timestamp")
                .query();
        meta.hydrateParentInstances(parentInstances);
    }

    // [新增] 暴露 versionDao 给 DatabaseTemplateStorage 使用
    public Dao<TemplateVersion, Integer> getVersionDao() {
        return versionDao;
    }
}
