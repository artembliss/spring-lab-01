package kz.iitu.springlab.aspect;

import java.time.Instant;
import java.util.Arrays;

import kz.iitu.springlab.audit.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        String arguments = audited.logArguments()
                ? " args=" + Arrays.toString(joinPoint.getArgs())
                : "";
        log.info("[AUDIT] start action={} timestamp={}{}",
                audited.action(), Instant.now(), arguments);
        try {
            Object result = joinPoint.proceed();
            log.info("[AUDIT] action={} timestamp={} outcome=success",
                    audited.action(), Instant.now());
            return result;
        } catch (Throwable ex) {
            log.error("[AUDIT] action={} timestamp={} outcome=failure error={}",
                    audited.action(), Instant.now(), ex.getMessage());
            throw ex;
        }
    }
}
