package com.project.shopapp.components.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.logging.Logger;

@Component
@Aspect
public class UserActivityLogger {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(UserActivityLogger.class);

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    @Around("controllerMethods() && execution(* com.project.shopapp.controllers.UserController.*(..))")
    public Object logUserActivity(ProceedingJoinPoint joinPoint) throws Throwable {
        // Ghi log trước khi thực hiện method
        String methodName = joinPoint.getSignature().getName();
        String remoteAddress = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRemoteAddr();

        logger.info("User activity started: " + methodName + ", IP address: " + remoteAddress);

        // Thực hiện method gốc
        Object result = joinPoint.proceed();

        // Ghi log sau khi thực hiện method
        logger.info("User activity finished: " + methodName);

        return result;
    }
}
