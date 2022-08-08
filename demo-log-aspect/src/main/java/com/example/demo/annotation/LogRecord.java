package com.example.demo.annotation;

import com.example.demo.util.UserContext;

import java.lang.annotation.*;

/**
 * 操作日志记录注解
 * <p>
 * 用于标注方法，在方法执行完后被调用。除 category 属性都支持使用 SpEL 表达式。
 *
 * @author snow-zen
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecord {

    /**
     * 成功消息，在没有异常抛出的情况下被使用。
     */
    String value();

    /**
     * 操作人，默认情况下使用 {@link UserContext#getLocalUserInfo()} 返回值作为操作人。
     * 当调用对应方法也不存在操作人信息时则使用 {@code Unknown} 作为默认值。
     */
    String operator() default "Unknown";

    /**
     * 消息分类，默认为 default。
     */
    String category() default "default";

    /**
     * 日志消息生效的条件。
     */
    String condition() default "true";
}
