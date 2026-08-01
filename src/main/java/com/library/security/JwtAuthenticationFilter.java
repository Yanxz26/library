package com.library.security;

import cn.hutool.core.util.StrUtil;
import com.library.common.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 *
 * @author Library Team
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        if (StrUtil.isNotBlank(token)) {
            try {
                if (jwtUtil.validateToken(token)) {
                    String userAccount = jwtUtil.getClaimsFromToken(token).getSubject();
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userAccount);
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        if (jwtUtil.needRefresh(token)) {
                            String newToken = jwtUtil.refreshToken(token);
                            response.setHeader("New-Token", newToken);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("JWT认证处理异常: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
