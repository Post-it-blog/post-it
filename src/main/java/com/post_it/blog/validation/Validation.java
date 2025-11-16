package com.post_it.blog.validation;

import java.util.regex.Pattern;

public class Validation {

    // 정규표현식
    private static final String ID_REGEX = "^[A-Za-z0-9]{5,16}$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=])\\S{8,16}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String NAME_REGEX = "^[A-Za-z가-힣]{2,30}$";
    private static final String NICKNAME_REGEX = "^[A-Za-z가-힣0-9_-]{4,20}$";
    private static final String EMAIL_AUTH_REGEX = "^[0-9]+$";

    // Pattern
    private static final Pattern ID_PATTERN = Pattern.compile(ID_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEX);
    private static final Pattern EMAIL_AUTH_PATTERN = Pattern.compile(EMAIL_AUTH_REGEX);

    // 체크 메서드
    public static boolean idCheck(String id) {
        if (id == null) return false;
        return ID_PATTERN.matcher(id).matches();
    }

    public static boolean passwordCheck(String pwd) {
        if (pwd == null) return false;
        return PASSWORD_PATTERN.matcher(pwd).matches();
    }

    public static boolean emailCheck(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean nameCheck(String name) {
        if (name == null) return false;
        return NAME_PATTERN.matcher(name).matches();
    }

    public static boolean nickCheck(String nick) {
        if (nick == null) return false;
        return NICKNAME_PATTERN.matcher(nick).matches();
    }

    public static boolean emailAuthCheck(String auth) {
        if (auth == null) return false;
        return EMAIL_AUTH_PATTERN.matcher(auth).matches();
    }
}

