package cn.taixue.comstats.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ne_pe_product_stat_logs")
public class NePeProductStatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ne_pe_product_log_id")
    private Long nePeProductLogId;

    @Column(name = "dau")
    private Integer dau;

    @Column(name = "avg_first_type_buy")
    private Double avgFirstTypeBuy;

    @Column(name = "avg_first_type_diamond")
    private Double avgFirstTypeDiamond;

    @Column(name = "avg_first_type_focus")
    private Double avgFirstTypeFocus;

    @Column(name = "avg_first_type_role_play")
    private Double avgFirstTypeRolePlay;

    @Column(name = "avg_playtime")
    private Double avgPlaytime;

    @Column(name = "avg_total_first_type_buy")
    private Double avgTotalFirstTypeBuy;

    @Column(name = "cnt_buy")
    private Integer cntBuy;

    @Column(name = "date_id")
    private String dateId;

    @Column(name = "date_ms")
    private Long dateMs;

    @Column(name = "diamond")
    private Integer diamond;

    @Column(name = "download_num")
    private Integer downloadNum;

    @Column(name = "first_type_avg_role_time")
    private Double firstTypeAvgRoleTime;

    @Column(name = "focus_cnt")
    private Integer focusCnt;

    @Column(name = "iid")
    private String iid;

    @Column(name = "iid_int")
    private Long iidInt;

    @Column(name = "pass_avg_role_time_ratio")
    private Double passAvgRoleTimeRatio;

    @Column(name = "pass_buy_cnt_ratio")
    private Double passBuyCntRatio;

    @Column(name = "pass_cnt_role_play_ratio")
    private Double passCntRolePlayRatio;

    @Column(name = "pass_focus_cnt_ratio")
    private Double passFocusCntRatio;

    @Column(name = "pass_pay_diamond_ratio")
    private Double passPayDiamondRatio;

    @Column(name = "platform")
    private String platform;

    @Column(name = "points")
    private Integer points;

    @Column(name = "refund_rate")
    private Double refundRate;

    @Column(name = "res_name")
    private String resName;

    @Column(name = "star_adjusted")
    private Double starAdjusted;

    @Column(name = "upload_time")
    private String uploadTime;

    @Column(name = "upload_time_ms")
    private Long uploadTimeMs;

}
