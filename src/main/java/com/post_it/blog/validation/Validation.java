package com.post_it.blog.validation;

import java.util.regex.Pattern;

public class Validation {

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\-+=])[a-zA-Z\\d!@#$%^&*()_\\-+=]{8,16}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public static boolean passwordCheck(String pwd) {
        if (pwd == null) return false;
        return PASSWORD_PATTERN.matcher(pwd).matches();
    }
}
