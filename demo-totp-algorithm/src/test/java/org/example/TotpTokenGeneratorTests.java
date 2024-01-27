package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * @author snow-zen
 */
public class TotpTokenGeneratorTests {

    @Test
    void testTotpToken() {
        String key = "3132333435363738393031323334353637383930";
        Map<Long, String> testCase = Map.of(
                59L, "94287082",
                1111111109L, "07081804",
                1111111111L, "14050471",
                1234567890L, "89005924",
                2000000000L, "69279037",
                20000000000L, "65353130"
                );

        TotpTokenGenerator ott = new TotpTokenGenerator();

        for (Map.Entry<Long, String> entry : testCase.entrySet()) {
            String totpVal = ott.generateTotpToken(key, entry.getKey(), 8);
            Assertions.assertEquals(entry.getValue(), totpVal);
        }
    }
}
