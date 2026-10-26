package by.vstu.vsrpp.exam.studgroup.security;

import by.vstu.vsrpp.exam.studgroup.entity.AppUser;
import by.vstu.vsrpp.exam.studgroup.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AppUserRepository appUserRepository;

    @Value("${jwt.secret:secret-key}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:3600000}")
    private long jwtExpirationMs;

    public SecurityConfig(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            AppUser appUser = appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return org.springframework.security.core.userdetails.User.builder()
                    .username(appUser.getUsername())
                    .password(appUser.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + appUser.getRole())))
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions().sameOrigin())
                .authorizeRequests(requests -> requests
                        .antMatchers("/api/auth/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                        )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // DaoAuthenticationProvider (UserDetailsService + PasswordEncoder)
    // Spring Security собирает из бинов автоматически
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider(), userDetailsService());
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(jwtSecret, jwtExpirationMs);
    }
}
