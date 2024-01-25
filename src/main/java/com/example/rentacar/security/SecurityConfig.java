package com.example.rentacar.security;

import com.example.rentacar.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Configures the security filter chain.
     *
     * @param http The HttpSecurity instance to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().
                authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/**").permitAll().and().
                authorizeRequests().
                antMatchers("/login",
                        "/register",
                        "/files/download/**",
                        "/contactmessage/visitors",
                        "/files/display/**",
                        "/car/visitors/**",
                        "actuator/info,",
                        "actuator/health").permitAll().
                anyRequest().authenticated();
        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS) for the application.
     *
     * @return WebMvcConfigurer instance with CORS configuration.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*").
                        allowedHeaders("*").allowedMethods("*");
            }
        };
    }


    private static final String[] AUTH_WHITE_LIST = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/",
            "/images/*",
            "/css/**",
            "/js/**"
    };

    /**
     * Configures a {@link WebSecurityCustomizer} bean to customize the behavior of Spring Security for web requests.
     * This customization is designed to ignore certain URL patterns during security checks.
     *
     * @return The configured {@link WebSecurityCustomizer} bean.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        WebSecurityCustomizer customizer = new WebSecurityCustomizer() {
            @Override
            public void customize(WebSecurity web) {
                web.ignoring().antMatchers(AUTH_WHITE_LIST);
            }
        };
        return customizer;
    }

    /**
     * Configures and provides an instance of the {@link AuthTokenFilter}.
     * The {@code AuthTokenFilter} is responsible for filtering and processing authentication tokens
     * in the incoming HTTP requests to secure the application.
     *
     * @return An instance of the {@link AuthTokenFilter}.
     */
    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Configures and provides an instance of the {@link BCryptPasswordEncoder}.
     * The {@code BCryptPasswordEncoder} is a password encoder that uses the BCrypt strong hashing function
     * to securely hash and verify passwords.
     *
     * @return An instance of the {@link BCryptPasswordEncoder} with a strength of 10.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Configures and provides an instance of {@link DaoAuthenticationProvider}.
     * The {@code DaoAuthenticationProvider} is an authentication provider that
     * authenticates users using a {@link UserDetailsService} for user retrieval
     * and a {@link PasswordEncoder} for password validation.
     *
     * @return An instance of {@link DaoAuthenticationProvider} configured with
     *         the application's {@link UserDetailsService} and {@link BCryptPasswordEncoder}.
     */
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    /**
     * Configures and provides an instance of {@link AuthenticationManager}.
     * The {@code AuthenticationManager} is responsible for authenticating
     * users using the configured {@link DaoAuthenticationProvider}.
     *
     * @param http The {@link HttpSecurity} instance shared by the application.
     * @return An instance of {@link AuthenticationManager} configured with the
     *         application's {@link DaoAuthenticationProvider}.
     * @throws Exception If an error occurs during the configuration process.
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class).
                authenticationProvider(authProvider()).
                build();
    }

}
