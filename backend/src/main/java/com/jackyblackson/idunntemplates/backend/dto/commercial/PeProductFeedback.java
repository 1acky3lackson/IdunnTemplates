package com.jackyblackson.idunntemplates.backend.dto.commercial;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PeProductFeedback {
    @JsonProperty("_id")
    private String id;

    @JsonProperty("commit_nickname")
    private String commitNickname;

    @JsonProperty("commit_uid")
    private String commitUid;

    private String content;

    @JsonProperty("create_time")
    private long createTime;

    @JsonProperty("feedback_log_file")
    private String feedbackLogFile;

    @JsonProperty("forbid_reply")
    private Boolean forbidReply;

    @JsonProperty("have_log_file")
    private Boolean haveLogFile;

    private String iid;

    @JsonProperty("pic_list")
    private Object picList;

    @JsonProperty("res_name")
    private String resName;

    private String type;
}
