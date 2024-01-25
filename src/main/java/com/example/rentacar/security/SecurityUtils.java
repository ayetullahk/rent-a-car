package com.example.rentacar.security;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public class SecurityUtils {

    /**
     * Retrieves the login (username or email) of the currently authenticated user.
     *
     * @return An {@link Optional} containing the login of the currently authenticated user,
     *         or an empty {@link Optional} if the user is not authenticated.
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();

        Authentication authentication = securityContext.getAuthentication();

        return Optional.ofNullable(extractPrincipal(authentication));

    }

    /**
     * Extracts the principal (login) from the given Authentication object.
     *
     * @param authentication The Authentication object from which to extract the principal.
     * @return The login (username or email) of the principal, or {@code null} if the principal
     *         cannot be extracted or is not available.
     */
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
