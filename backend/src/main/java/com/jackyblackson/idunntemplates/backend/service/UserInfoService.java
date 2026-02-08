package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.LuckyUserInfo;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final TemplateVersionRepository templateVersionRepository;
    private final LuckyPermUserInfoService luckyPermUserInfoService;

    /**
     * 获取所有模板版本提交者的用户信息
     * 优化：直接通过 JPA 投影查询获取去重后的 Submitter ID，避免加载大批量实体对象
     */
    @Transactional(readOnly = true)
    public List<LuckyUserInfo> getAllTemplateCreators() {
        // 1. 利用 ORM 投影查询直接获取唯一的 UUID 列表
        // 数据库层面执行: SELECT DISTINCT submitter_id ...
        List<UUID> uniqueSubmitterUuids = templateVersionRepository.findDistinctSubmitterIds();

        if (uniqueSubmitterUuids.isEmpty()) {
            log.debug("No submitter IDs found in template versions.");
            return List.of();
        }

        // 2. 转换为 String 列表以匹配 LuckyPerms 批量接口的参数
        List<String> uuidStrings = uniqueSubmitterUuids.stream()
                .map(UUID::toString)
                .collect(Collectors.toList());

        log.info("Batch querying LuckyPerms info for {} unique submitters.", uuidStrings.size());

        // 3. 调用 LuckyPerms 接口进行批量查询
        List<LuckyUserInfo> rawUserInfoList = luckyPermUserInfoService.getBulkUserInfo(uuidStrings);

        // 4. 去除查询失败 (null) 的项并返回
        return rawUserInfoList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}