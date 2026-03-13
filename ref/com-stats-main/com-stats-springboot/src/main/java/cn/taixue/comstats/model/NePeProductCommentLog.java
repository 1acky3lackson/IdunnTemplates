package cn.taixue.comstats.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ne_pe_product_comment_logs")
public class NePeProductCommentLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "comment_id")
    private String commentId;

    @Column(name = "comment_state")
    private String commentState;

    @Column(name = "comment_tag")
    private String commentTag;

    @Column(name = "commented_num")
    private String commentedNum;

    @Column(name = "commented_num_int")
    private Long commentedNumInt;

    @Column(name = "good_num")
    private String goodNum;

    @Column(name = "good_num_int")
    private Long goodNumInt;

    @Column(name = "iid")
    private String iid;

    @Column(name = "iid_int")
    private Long iidInt;

    @Column(name = "master_id")
    private String masterId;

    @Column(name = "master_id_int")
    private Long masterIdInt;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "publish_time")
    private String publishTime;

    @Column(name = "publish_time_ms")
    private Long publishTimeMs;

    @Column(name = "reply_id")
    private String replyId;

    @Column(name = "reply_id_int")
    private Long replyIdInt;

    @Column(name = "res_name")
    private String resName;

    @Column(name = "stars")
    private String stars;

    @Column(name = "stars_int")
    private Long starsInt;

    @Column(name = "uid")
    private String uid;

    @Column(name = "uid_int")
    private Long uidInt;

    @Column(name = "user_comment", columnDefinition = "text")
    private String userComment;

}
