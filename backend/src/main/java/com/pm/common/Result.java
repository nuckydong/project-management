package com.pm.common;

public class Result<T> {

    private int code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> success() {
        return (Result<T>) new Result<Void>(200, "success", null);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> error(String message) {
        return (Result<T>) new Result<Void>(500, message, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> error(int code, String message) {
        return (Result<T>) new Result<Void>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
