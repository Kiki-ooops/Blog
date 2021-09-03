package com.kiki.blog.app.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
public class LikeCommentEntity {
    private UUID id;
    private UserEntity user;
    private CommentEntity comment;

    public LikeCommentEntity(UserEntity user, CommentEntity comment) {
        this.user = user;
        this.comment = comment;
    }

    public LikeCommentEntity() {

    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY, generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    public UUID getId() {
        return id;
    }

    @ManyToOne
    public UserEntity getUser() {
        return user;
    }
    @ManyToOne
    public CommentEntity getComment() {
        return comment;
    }
}
