package cn.taixue.comstats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PeProductStat {
    @JsonProperty("DAU")
    private Integer dau;

    @JsonProperty("avg_first_type_buy")
    private Double avgFirstTypeBuy;

    @JsonProperty("avg_first_type_diamond")
    private Double avgFirstTypeDiamond;

    @JsonProperty("avg_first_type_focus")
    private Double avgFirstTypeFocus;

    @JsonProperty("avg_first_type_role_play")
    private Double avgFirstTypeRolePlay;

    @JsonProperty("avg_playtime")
    private Double avgPlaytime;

    @JsonProperty("avg_total_first_type_buy")
    private Double avgTotalFirstTypeBuy;

    @JsonProperty("cnt_buy")
    private Integer cntBuy;

    private String dateid;

    private Integer diamond;

    @JsonProperty("download_num")
    private Integer downloadNum;

    @JsonProperty("first_type_avg_role_time")
    private Double firstTypeAvgRoleTime;

    @JsonProperty("focus_cnt")
    private Integer focusCnt;

    private String iid;

    @JsonProperty("pass_avg_role_time_ratio")
    private Double passAvgRoleTimeRatio;

    @JsonProperty("pass_buy_cnt_ratio")
    private Double passBuyCntRatio;

    @JsonProperty("pass_cnt_role_play_ratio")
    private Double passCntRolePlayRatio;

    @JsonProperty("pass_focus_cnt_ratio")
    private Double passFocusCntRatio;

    @JsonProperty("pass_pay_diamond_ratio")
    private Double passPayDiamondRatio;

    private String platform;

    private Integer points;

    @JsonProperty("refund_rate")
    private Double refundRate;

    @JsonProperty("res_name")
    private String resName;

    @JsonProperty("star_adjusted")
    private Double starAdjusted;

    @JsonProperty("upload_time")
    private String uploadTime;
}
