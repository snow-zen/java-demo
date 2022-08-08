package com.example.demo.aspect;

import com.example.demo.annotation.LogRecord;
import com.example.demo.service.LogService;
import com.example.demo.util.UserContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志切面
 *
 * @author snow-zen
 */
@Aspect
@Component
public class LogRecordAspect {

    /**
     * 日志服务
     */
    private final LogService logService;

    /**
     * 日志专属 SpEL 表达式计算器
     */
    private final LogRecordExpressionEvaluator evaluator = new LogRecordExpressionEvaluator();

    public LogRecordAspect(LogService logService) {
        this.logService = logService;
    }

    /**
     * 切面逻辑实现
     *
     * @param point 切点谢谢
     * @return 切点方法实际返回值
     * @throws Throwable 切点调用失败抛出
     */
    @Around("@annotation(com.example.demo.annotation.LogRecord)")
    public Object exec(ProceedingJoinPoint point) throws Throwable {
        if (!(point.getSignature() instanceof MethodSignature)) {
            throw new UnsupportedOperationException("@LogRecord 注解仅支持在方法上标注");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // 执行方法
        Object result = point.proceed();

        LogRecord logRecord = method.getAnnotation(LogRecord.class);
        EvaluationContext context = evaluator.createEvaluationContext(method, point.getArgs(), result);
        if (evaluator.getCondition(logRecord.condition(), context)) {
            String messageTemplate = logRecord.value();
            AnnotatedElementKey key = new AnnotatedElementKey(method, point.getTarget().getClass());

            String message = evaluator.getMessage(messageTemplate, key, context);
            String category = logRecord.category();
            String operator = getOperator(logRecord, context);

            logService.saveLog(message, category, operator);
        }
        return result;
    }

    /**
     * 计算操作人信息
     */
    private String getOperator(LogRecord logRecord, EvaluationContext context) {
        final String unknownOperator = "Unknown";
        if (unknownOperator.equals(logRecord.operator())) {
            String userInfo = UserContext.getLocalUserInfo();
            return userInfo != null ? userInfo : unknownOperator;
        }
        return evaluator.getOperator(logRecord.operator(), context);
    }

    /**
     * 日志表达式计算器
     */
    private static class LogRecordExpressionEvaluator extends CachedExpressionEvaluator {
        private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        private final Map<ExpressionKey, Expression> conditionCache = new ConcurrentHashMap<>();

        public EvaluationContext createEvaluationContext(Method method, Object[] args, Object result) {
            LogRecordExpressionRootObject root = new LogRecordExpressionRootObject(method, args, result);
            return new MethodBasedEvaluationContext(root, method, args, parameterNameDiscoverer);
        }

        public String getMessage(String messageTemplate, AnnotatedElementKey methodKey, EvaluationContext evaluationContext) {
            return getExpression(conditionCache, methodKey, messageTemplate).getValue(evaluationContext, String.class);
        }

        public boolean getCondition(String expr, EvaluationContext evaluationContext) {
            return Boolean.TRUE.equals(parseExpression(expr).getValue(evaluationContext, Boolean.class));
        }

        public String getOperator(String expr, EvaluationContext evaluationContext) {
            return parseExpression(expr).getValue(evaluationContext, String.class);
        }

        @Override
        protected Expression parseExpression(String expression) {
            return getParser().parseExpression(expression);
        }
    }

    /**
     * 日志表达式 root 对象定义
     */
    @Getter
    @AllArgsConstructor
    private static class LogRecordExpressionRootObject {

        private final Method method;
        private final Object[] args;
        private final Object result;
    }
}
