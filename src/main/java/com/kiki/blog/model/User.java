package com.kiki.blog.model;

import lombok.Data;

@Data
public class User {
    private String email;
    private String username;
    private String password;
    private String roles;
}
