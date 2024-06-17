package org.example.newsfeed.logging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controller() {}

    @Before("controller()")
    public void logBefore(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        log.info("Request URL: {}, HTTP Method: {}, Controller: {}.{}",
            request.getRequestURL(),
            request.getMethod(),
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = "controller()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Response from {}.{}: {}",
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            result);
    }

    @AfterThrowing(pointcut = "controller()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("Exception in {}.{}: {}",
            joinPoint.getSignature().getDeclaringTypeName(),
            joinPoint.getSignature().getName(),
            exception.getMessage());
    }
}
