package com.example.demo.service;

import org.springframework.stereotype.Service;

/**
 * 日志服务
 *
 * @author snow-zen
 */
@Service
public class LogService {

    public void saveLog(String msg, String category, String operator) {
        System.out.printf("操作人【%s】：%s【分类：%s】%n", msg, category, operator);
    }
}
