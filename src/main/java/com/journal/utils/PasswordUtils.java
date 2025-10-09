package com.journal.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtils {

    public static String hashPassword(String plain) {
        // Convert to char[] before passing to hashToString
        return BCrypt.withDefaults().hashToString(12, plain.toCharArray());
    }

    public static boolean verifyPassword(String plain, String hashed) {
        // Convert plain password to char[] for verification
        BCrypt.Result result = BCrypt.verifyer().verify(plain.toCharArray(), hashed);
        return result.verified;
    }
}

