package com.tcmseek.tools;

import com.tcmseek.common.utils.SecurityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Console;
import java.util.Scanner;

/**
 * Local helper for generating RuoYi admin password hashes.
 */
public class AdminPasswordEncoderTest {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "792580Xx";

    @Test
    void printEncodedPassword() {
        String rawPassword = firstText(System.getProperty("password"), DEFAULT_PASSWORD);
        String username = firstText(System.getProperty("username"), DEFAULT_USERNAME);
        String encodedPassword = printEncodedPassword(username, rawPassword);

        Assertions.assertTrue(SecurityUtils.matchesPassword(rawPassword, encodedPassword));
    }

    public static void main(String[] args) {
        String rawPassword = resolveRawPassword(args);
        String username = firstText(System.getProperty("username"), DEFAULT_USERNAME);
        printEncodedPassword(username, rawPassword);
    }

    private static String resolveRawPassword(String[] args) {
        if (args != null && args.length > 0 && hasText(args[0])) {
            return args[0];
        }

        String propertyPassword = System.getProperty("password");
        if (hasText(propertyPassword)) {
            return propertyPassword;
        }

        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword("Input raw password: ");
            return chars == null ? "" : new String(chars);
        }

        System.out.print("Input raw password: ");
        return new Scanner(System.in).nextLine();
    }

    private static String printEncodedPassword(String username, String rawPassword) {
        if (!hasText(rawPassword)) {
            throw new IllegalArgumentException("Password must not be blank.");
        }

        String encodedPassword = SecurityUtils.encryptPassword(rawPassword);
        System.out.println();
        System.out.println("BCrypt password:");
        System.out.println(encodedPassword);
        System.out.println();
        System.out.println("SQL example:");
        System.out.println("UPDATE sys_user SET password = '" + encodedPassword + "' WHERE user_name = '" + username + "';");
        System.out.println();
        return encodedPassword;
    }

    private static String firstText(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
