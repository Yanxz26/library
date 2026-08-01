package com.library.common.aop;

import com.library.common.annotation.Log;
import com.library.common.utils.IpUtil;
import com.library.common.utils.JwtUtil;
import com.library.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 操作日志AOP切面
 *
 * @author Library Team
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Around("@annotation(com.library.common.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try {
                long elapsedTime = System.currentTimeMillis() - startTime;

                // 获取注解信息
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                Method method = signature.getMethod();
                Log logAnnotation = method.getAnnotation(Log.class);

                // 获取请求信息
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String requestUrl = request.getRequestURI();
                    String requestMethod = request.getMethod();
                    String ipAddr = IpUtil.getIpAddr(request);

                    // 获取当前用户ID
                    Long userId = null;
                    String token = request.getHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        userId = jwtUtil.getUserId(token.substring(7));
                    }

                    // 构建操作描述
                    String operation = logAnnotation.value();
                    if (operation.isEmpty()) {
                        operation = signature.getDeclaringTypeName() + "." + method.getName();
                    }

                    // 记录日志
                    if (errorMsg != null) {
                        sysLogService.saveLog(userId, logAnnotation.type(),
                                operation + " [异常: " + errorMsg + "]",
                                requestUrl, requestMethod, ipAddr, errorMsg);
                    } else {
                        sysLogService.saveLog(userId, logAnnotation.type(),
                                operation + " [耗时: " + elapsedTime + "ms]",
                                requestUrl, requestMethod, ipAddr, null);
                    }
                }
            } catch (Exception e) {
                log.error("记录操作日志失败: {}", e.getMessage());
            }
        }
    }
}
