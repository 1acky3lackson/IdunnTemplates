package com.jackyblackson.idunntemplates.backend.commercial.service;

import com.jackyblackson.idunntemplates.backend.commercial.dto.netease.NeteaseProductDto;
import com.jackyblackson.idunntemplates.backend.commercial.entity.netease.NeteaseProduct;
import com.jackyblackson.idunntemplates.backend.dto.UserContext;
import com.jackyblackson.idunntemplates.backend.service.LuckyPermAuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductPermissionService {
    private final LuckyPermAuthService luckyPermAuthService;

    /**
     * 将实体转换为 DTO，并根据权限隐藏字段
     */
    public NeteaseProductDto toDto(NeteaseProduct entity) {
        if (entity == null) return null;

        NeteaseProductDto dto = new NeteaseProductDto();
        // 先复制所有字段（可使用 BeanUtils.copyProperties 简化）
        org.springframework.beans.BeanUtils.copyProperties(entity, dto);

//        var permResult = luckyPermAuthService.batchCheckPermissions();

        // 如果没有权限，将敏感字段置为 null
        if (false) {
            dto.setPrice(null);
            dto.setDiscount(null);
            dto.setInterceptFields(null);
            dto.setLobbyConfigOpLog(null);
            dto.setLobbySortKey(null);
            dto.setPerfData(null);          // 如果有的话
            dto.setOrderPayload(null);
            dto.setStatPayload(null);
            // 其他需要隐藏的字段...
        }

        return dto;
    }

    /**
     * 批量转换
     */
    public List<NeteaseProductDto> toDtoList(Collection<NeteaseProduct> entities) {
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }
}
