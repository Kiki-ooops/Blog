package com.kiki.blog.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Data
@Entity
public class UserEntity {

    private UUID id;
    private String email;
    private String username;
    private String password;
    private String roles;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY, generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    public UUID getId() {
        return id;
    }
}
