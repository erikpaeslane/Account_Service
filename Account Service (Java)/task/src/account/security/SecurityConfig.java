package account.security;

import account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .httpBasic(httpBasic ->
                        httpBasic.authenticationEntryPoint(authenticationEntryPoint()) // Custom AuthenticationEntryPoint
                )
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(handing -> handing
                         // Handles auth error
                        .accessDeniedHandler(accessDeniedHandler())

                )
                // For modifying requests via Postman
                .authorizeHttpRequests(requests -> requests // manage access
                        .requestMatchers("/actuator/shutdown", "/error/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass")
                        .hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_USER", "ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment")
                                .hasAnyAuthority("ROLE_USER", "ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments").hasAuthority("ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments").hasAuthority("ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/admin/user/").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/user/{user}").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/role").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/access").hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/security/events/").hasAuthority("ROLE_AUDITOR")
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

}
