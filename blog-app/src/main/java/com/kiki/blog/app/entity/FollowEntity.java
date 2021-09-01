package com.kiki.blog.app.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
public class FollowEntity {
    private UUID id;
    private UserEntity from;
    private UserEntity to;

    public FollowEntity(UserEntity from, UserEntity to) {
        this.from = from;
        this.to = to;
    }

    public FollowEntity() {

    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY, generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    public UUID getId() {
        return id;
    }

    @ManyToOne
    public UserEntity getFrom() {
        return from;
    }
    @ManyToOne
    public UserEntity getTo() {
        return to;
    }
}
