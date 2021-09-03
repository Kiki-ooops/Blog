package com.kiki.blog.app.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
public class LikePostEntity {
    private UUID id;
    private UserEntity user;
    private PostEntity post;

    public LikePostEntity(UserEntity user, PostEntity post) {
        this.user = user;
        this.post = post;
    }

    public LikePostEntity() {

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
    public PostEntity getPost() {
        return post;
    }
}
