package cn.taixue.comstats.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ne_pe_product_feedback_logs")
public class NePeProductFeedbackLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "feedback_id")
    private String feedbackId;

    @Column(name = "commit_nickname")
    private String commitNickname;

    @Column(name = "commit_uid")
    private String commitUid;

    @Column(name = "commit_uid_int")
    private Long commitUidInt;

    @Column(name = "content", columnDefinition = "text")
    private String content;

    @Column(name = "create_time_ms")
    private Long createTimeMs;

    @Column(name = "feedback_log_file")
    private String feedbackLogFile;

    @Column(name = "forbid_reply")
    private Boolean forbidReply;

    @Column(name = "have_log_file")
    private Boolean haveLogFile;

    @Column(name = "iid")
    private String iid;

    @Column(name = "iid_int")
    private Long iidInt;

    @Column(name = "pic_list", columnDefinition = "text")
    private String picList;

    @Column(name = "res_name")
    private String resName;

    @Column(name = "type")
    private String type;

}
