import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class PerformanceMonitorAspect {

    public PerformanceMonitorAspect() {
        // constructor, prints a message when the aspect is initialized
        System.out.println("PerformanceMonitorAspect initialized.");
    }

    @Around("execution(* *..*Servlet.*(..))")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        // captures the start time (before the method executes)
        long start = System.currentTimeMillis();

        // proceed with the actual method invocation
        Object result = joinPoint.proceed();

        // calculate elapsed time by subtracting start time from current time
        long elapsedTime = System.currentTimeMillis() - start;

        // log the method signature and how long it took to execute, will appear in tomcat cmd window
        System.out.println(joinPoint.getSignature() + " executed in " + elapsedTime + " ms");

        // return the result of the method execution (so it proceeds as normal)
        return result;
    }
}
