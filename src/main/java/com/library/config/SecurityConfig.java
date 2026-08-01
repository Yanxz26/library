package com.library.config;

import com.library.security.JwtAuthenticationFilter;
import com.library.security.handler.AccessDeniedHandlerImpl;
import com.library.security.handler.AuthenticationEntryPointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置
 *
 * @author Library Team
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 关闭CSRF
            .csrf().disable()
            // 禁用Session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 异常处理
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            .and()
            // 请求权限配置
            .authorizeRequests()
                // 允许匿名访问
                .antMatchers("/auth/login", "/auth/register", "/auth/forgot-password/**").permitAll()
                .antMatchers("/", "/index.html", "/static/**", "/*.html", "/*.js", "/*.css").permitAll()
                .antMatchers("/upload/**", "/uploads/**", "/api/upload/**", "/api/uploads/**").permitAll()
                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                        "/doc.html", "/webjars/**", "/swagger-resources/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 统计API对所有已认证用户开放
                .antMatchers("/sys/statistics/**").authenticated()
                // 图书查询API对所有已认证用户开放
                .antMatchers("/sys/book/page", "/sys/book/hot", "/sys/book/new", "/sys/book/detail/**").authenticated()
                // 分类查询API对所有已认证用户开放
                .antMatchers("/sys/category/tree").authenticated()
                // 个人信息API对所有已认证用户开放
                .antMatchers("/sys/user/profile", "/sys/user/detail/**").authenticated()
                // 管理员专属接口
                .antMatchers("/sys/**").hasAnyRole("admin", "library")
                // 其他接口需要认证
                .anyRequest().authenticated()
            .and()
            // JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
