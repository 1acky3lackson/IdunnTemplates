package com.jackyblackson.idunntemplates.backend.commercial.entity.checkout;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "commercial_checkout_context_global")
@NoArgsConstructor
public class GlobalCheckoutParamContext {
    public GlobalCheckoutParamContext(String username) {
        this.createUsername = username;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 太学抽取的比例。也就是说总金额为 x 的订单，太学获取 taixueRatio * x，参与者获取 (1 - taixueRatio) * x。
     */
    @Column(name = "taixue_ratio")
    Double taixueRatio = 0.3;

    /**
     * 商务处抽取的比例。也就是说所有参与者获得总额为 x 的订单，商务处获取 commercialRatio * x，参与者获取 (1 - commercialRatio) * x。
     */
    @Column(name = "commercial_ratio")
    Double commercialRatio = 0.3;

    /**
     * 假设模板的有效方块数量为 x，则第 i 次使用时，其计算后的方块树是 x * (templateDefectParam ** (i - 1))，i 从 1 开始。
     */
    @Column(name = "template_defect_param")
    Double templateDefectParam = 0.8;

    /**
     * 模板放置者在模板中的贡献比例。假设模板收入为 x，那么放置者或者 placerRatio * x，模板作者获得 (1 - placerRatio) * x。
     */
    @Column(name = "placer_ratio")
    Double placerRatio = 0.4;

    @Column(name = "uploader_ratio")
    Double uploaderRatio = 0.3;

    @Column(name = "create_username")
    String createUsername = "system";

    @Column (name = "create_time_ms")
    Long createTimeMs = System.currentTimeMillis();

    @Column (name = "disable_time_ms")
    Long disableTimeMs;

    @Column (name = "disable_reason")
    String disableReason;

    public boolean isDisabled() {
        return this.disableReason != null;
    }
}
