package com.kiki.blog.app.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
public class CommentEntity {
    private UUID id;
    private String content;
    private OffsetDateTime datetime;
    private UserEntity user;
    private PostEntity post;

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
    public PostEntity getPost() {
        return post;
    }
}
