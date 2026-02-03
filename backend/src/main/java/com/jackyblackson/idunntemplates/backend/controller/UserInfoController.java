package com.jackyblackson.idunntemplates.backend.controller;

import com.jackyblackson.idunntemplates.backend.dto.LuckyUserInfo;
import com.jackyblackson.idunntemplates.backend.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 玩家信息查询接口
 */
@RestController
@RequestMapping("/api/v1/userinfo")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    /**
     * 获取所有在模板库中出现过的创作者信息
     * GET /api/v1/userinfo/creators
     * * @return 创作者信息列表 (包含 name 和 uuid)
     */
    @GetMapping("/creators")
    public ResponseEntity<List<LuckyUserInfo>> getAllCreators() {
        // 调用 Service 获取去重且排除 null 后的创作者列表
        List<LuckyUserInfo> creators = userInfoService.getAllTemplateCreators();

        // 返回 200 OK 以及数据列表
        return ResponseEntity.ok(creators);
    }
}
