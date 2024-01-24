package com.example.rentacar.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityUtils {//controller katmanında anlık olarak login olan kullanıcıya ihtiyac olması halinde burada bulanan metod kullanmak

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();

        Authentication authentication = securityContext.getAuthentication();

        return Optional.ofNullable(extractPrincipal(authentication));

    }

    private static String extractPrincipal(Authentication authentication){
        if (authentication==null){
            return null;
        } else if (authentication.getPrincipal()instanceof UserDetails) {
            UserDetails secureUser= (UserDetails) authentication.getPrincipal();
            return secureUser.getUsername();
        } else if (authentication.getPrincipal()instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

}
