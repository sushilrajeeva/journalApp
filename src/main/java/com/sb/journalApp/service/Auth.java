package com.sb.journalApp.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;


public final class Auth {
    private Auth() {}
    public static Long currentUserId() {
        var context = SecurityContextHolder.getContext();

        var authentication = context.getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException((HttpStatus.UNAUTHORIZED), "Missing or invalid token!");
        }
        var uid = jwt.getClaim("uid");
        if (uid == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token missing uid");
        return (uid instanceof Number n) ? n.longValue() : Long.parseLong(uid.toString());
    }
}

