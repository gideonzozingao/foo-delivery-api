// ============= SecurityConstants.java =============
package com.zuqongtch.security;

public class SecurityConstants {
    public static final String JWT_SECRET = "your-secret-key-change-this-in-production-make-it-very-long-and-secure";
    public static final long JWT_EXPIRATION = 86400000; // 24 hours in milliseconds
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    private SecurityConstants() {
        // Prevent instantiation
    }
}
