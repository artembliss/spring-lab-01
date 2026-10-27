package kz.iitu.springlab.aspect;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(4)
public class CallCounterAspect {
    private static final Logger log = LoggerFactory.getLogger(CallCounterAspect.class);
    private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();

    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void count(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().getName();
        LongAdder counter = counters.computeIfAbsent(method, ignored -> new LongAdder());
        counter.increment();
        log.info("[COUNT] method={} calls={}", method, counter.sum());
    }

    public Map<String, Long> statistics() {
        Map<String, Long> snapshot = new TreeMap<>();
        counters.forEach((method, count) -> snapshot.put(method, count.sum()));
        return snapshot;
    }
}
