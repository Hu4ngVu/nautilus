package vn.io.huangnosimp.nautilusserver.logging;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@annotation(logging)")
    public Object logMethod(ProceedingJoinPoint joinPoint, Logging logging) throws Throwable {
        String method = methodName(joinPoint);
        String arguments =
                logging.logArguments() ? " args=" + Arrays.toString(joinPoint.getArgs()) : "";
        long startedAt = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long durationMillis = (System.nanoTime() - startedAt) / 1_000_000;
            write(
                    logging.level(),
                    "Completed " + method + " in " + durationMillis + " ms." + "with" + arguments);
            return result;
        } catch (Throwable exception) {
            long durationMillis = (System.nanoTime() - startedAt) / 1_000_000;
            log.error("Failed {} in {} ms", method, durationMillis, exception);
            throw exception;
        }
    }

    private String methodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
    }

    private void write(Level level, String message) {
        switch (level) {
            case TRACE -> log.trace(message);
            case DEBUG -> log.debug(message);
            case INFO -> log.info(message);
            case WARN -> log.warn(message);
            case ERROR -> log.error(message);
        }
    }
}
