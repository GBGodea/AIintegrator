package com.godea.authorization.config;

import com.godea.authorization.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(auth ->
                        auth.requestMatchers(HttpMethod.POST, "/api/users", "/api/auth").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/roles").hasAuthority(Constants.Roles.ADMIN)
                                .requestMatchers(HttpMethod.DELETE, "/api/roles/**").hasAuthority(Constants.Roles.ADMIN)
                                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyAuthority(Constants.Roles.USER, Constants.Roles.ADMIN)
                                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                                .anyRequest().denyAll())
                .sessionManagement(e -> e.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(logout -> logout.logoutUrl("/api/auth/logout").logoutSuccessHandler(((request, response, authentication) -> {
                    Cookie cookie = new Cookie("jwt", "");
                    cookie.setHttpOnly(true);
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    response.setStatus(HttpServletResponse.SC_OK);
                })))
                .formLogin(AbstractHttpConfigurer::disable)
                .cors(e -> e.configurationSource(corsPolicyFrontend()))
                .csrf(AbstractHttpConfigurer::disable);
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth"));

        httpSecurity.addFilterBefore(jwtCookieFilter(), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    private UrlBasedCorsConfigurationSource corsPolicyFrontend() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/api/users", configuration);
//        source.registerCorsConfiguration("/api/users/**", configuration);
//        source.registerCorsConfiguration("/api/auth", configuration);
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtCookieFilter jwtCookieFilter() {
        return new JwtCookieFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserService();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
        return authenticationManagerBuilder.build();
    }
}
