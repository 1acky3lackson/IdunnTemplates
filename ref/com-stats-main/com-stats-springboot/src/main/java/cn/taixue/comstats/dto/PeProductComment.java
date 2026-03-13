package cn.taixue.comstats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PeProductComment {
    @JsonProperty("_id")
    private String id;

    @JsonProperty("comment_state")
    private String commentState;

    @JsonProperty("comment_tag")
    private String commentTag;

    @JsonProperty("commented_num")
    private String commentedNum;

    @JsonProperty("good_num")
    private String goodNum;

    private String iid;

    @JsonProperty("master_id")
    private String masterId;

    private String nickname;

    @JsonProperty("publish_time")
    private String publishTime;

    @JsonProperty("reply_id")
    private String replyId;

    @JsonProperty("res_name")
    private String resName;

    private String stars;

    private String uid;

    @JsonProperty("user_comment")
    private String userComment;
}
