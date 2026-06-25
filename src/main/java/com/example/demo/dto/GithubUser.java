package com.example.demo.dto;

public class GithubUser {
    public Long id;
    public String email;
    public String name;
    public String login;

    public String getDisplayName() {
        return (name != null && !name.isEmpty()) ? name : login;
    }
}
