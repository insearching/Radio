package com.sj.radio.app.entity;

public class AuthResponse {

    private int code;
    private String token;
    private String message;

    public AuthResponse() {
        code = -1;
        token = null;
        message = null;
    }

    public int getCode() {
        return code;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}