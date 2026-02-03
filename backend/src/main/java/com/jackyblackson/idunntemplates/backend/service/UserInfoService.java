package com.jackyblackson.idunntemplates.backend.service;

import com.jackyblackson.idunntemplates.backend.dto.LuckyUserInfo;
import com.jackyblackson.idunntemplates.backend.store.repository.TemplateRepository;
import com.jackyblackson.idunntemplates.core.domain.Template;
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

    private final TemplateRepository templateRepository;
    private final LuckyPermUserInfoService luckyPermUserInfoService;

    /**
     * 获取所有模板创作者的用户信息
     * * @return 包含玩家名称和 UUID 的信息列表，已去重且排除了无法查询到的用户
     */
    @Transactional(readOnly = true)
    public List<LuckyUserInfo> getAllTemplateCreators() {
        // 1. 从数据库中获取所有模板（注意：大型数据库建议只查询 creator_id 字段以优化性能）
        List<Template> allTemplates = templateRepository.findAll();

        // 2. 提取并去重所有的 Creator UUID
        List<String> uniqueCreatorUuids = allTemplates.stream()
                .map(t -> t.getMetadata().getCreatorId())
                .filter(Objects::nonNull)
                .map(UUID::toString)
                .distinct() // 关键：去重，避免重复查询同一个作者
                .collect(Collectors.toList());

        if (uniqueCreatorUuids.isEmpty()) {
            return List.of();
        }

        // 3. 调用 LuckyPerms 接口进行批量查询
        List<LuckyUserInfo> rawUserInfoList = luckyPermUserInfoService.getBulkUserInfo(uniqueCreatorUuids);

        // 4. 去除查询失败 (null) 的项并返回
        return rawUserInfoList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}