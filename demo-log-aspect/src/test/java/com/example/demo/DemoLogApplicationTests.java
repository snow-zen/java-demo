package com.example.demo;

import com.example.demo.service.CandySaveRequest;
import com.example.demo.service.CandyService;
import com.example.demo.service.LogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
class DemoLogApplicationTests {

    @Autowired
    private CandyService candyService;

    @MockBean
    private LogService logService;

    @Test
    void testLog() {
        CandySaveRequest request = new CandySaveRequest();
        request.setCandyId(1);
        request.setCandyName("best");

        Mockito.doAnswer(an -> {
            String message = an.getArgument(0, String.class);
            assertTrue(message != null && !message.trim().isEmpty());
            assertTrue(message.contains(String.valueOf(request.getCandyId())));
            assertTrue(message.contains(request.getCandyName()));

            assertEquals("candy", an.getArgument(1, String.class));
            assertEquals("Unknown", an.getArgument(2, String.class));
            return null;
        }).when(logService).saveLog(anyString(), anyString(), anyString());

        candyService.saveCandy(request);
    }

}
