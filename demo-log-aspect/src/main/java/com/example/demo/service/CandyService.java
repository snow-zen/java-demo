package com.example.demo.service;

import com.example.demo.annotation.LogRecord;
import org.springframework.stereotype.Service;

/**
 * @author snow-zen
 */
@Service
public class CandyService {

    @LogRecord(value = "'保存糖果成功，糖果id为【' + #request.candyId + '】，糖果名为【' + #request.candyName + '】'",
            category = "candy")
    public void saveCandy(CandySaveRequest request) {
        System.out.println(request);
    }
}
