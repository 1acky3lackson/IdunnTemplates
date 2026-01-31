package com.jackyblackson.idunntemplates.core.store.dao;

import com.jackyblackson.idunntemplates.core.domain.Instance;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class InstanceDao extends BaseDaoImpl<Instance, String> {

    public InstanceDao(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Instance.class);
    }

    /**
     * 查找某个 Template 下的所有活跃（未删除）实例
     */
    public List<Instance> findActiveByTemplate(UUID templateId) throws SQLException {
        return queryBuilder()
                .where()
                .eq("template_id", templateId)
                .and()
                .isNull("deleted_timestamp") // 只查没被软删除的
                .query();
    }

    /**
     * 查找属于某个父模板（嵌套）的所有实例
     */
    public List<Instance> findByParentTemplate(UUID parentTemplateId) throws SQLException {
        return queryBuilder()
                .where()
                .eq("embedded_in_template_id", parentTemplateId)
                .and()
                .isNull("deleted_timestamp")
                .query();
    }

    /**
     * 空间范围查询：查找位于指定世界和坐标范围内的所有实例。
     * 这通常用于判断是否与现有的 Template 重叠。
     *
     * @param worldId 世界 ID
     * @param minX 最小 X
     * @param minZ 最小 Z
     * @param maxX 最大 X
     * @param maxZ 最大 Z
     */
    public List<Instance> findInRegion(UUID worldId, int minX, int minZ, int maxX, int maxZ) throws SQLException {
        QueryBuilder<Instance, String> qb = queryBuilder();
        Where<Instance, String> where = qb.where();

        // 基础条件：世界匹配且未删除
        where.eq("world_id", worldId)
                .and()
                .isNull("deleted_timestamp");

        // 空间条件：简单矩形过滤
        // 注意：这里查询的是 Instance 的锚点 (x, y, z)。
        // 如果你需要查询“Instance 的体积是否与区域重叠”，仅查锚点是不够的，
        // 但通常我们先查出附近的锚点，再在内存里精确计算体积重叠（因为数据库不知道每个 Instance 的宽高）。
        where.and().ge("x", minX)
                .and().le("x", maxX)
                .and().ge("z", minZ)
                .and().le("z", maxZ);

        return qb.query();
    }

    /**
     * 查找某个玩家放置的所有实例（按时间倒序）
     */
    public List<Instance> findByPlayer(UUID playerUuid, int limit) throws SQLException {
        return queryBuilder()
                .orderBy("placed_at", false) // false = 降序
                .limit((long) limit)
                .where()
                .eq("placed_by", playerUuid)
                .and()
                .isNull("deleted_timestamp")
                .query();
    }
}
