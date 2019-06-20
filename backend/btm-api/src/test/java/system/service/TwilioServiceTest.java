package system.service;

import org.junit.Test;

public class TwilioServiceTest {

    @Test
    public void testTextFormattedRight() {
        new TwilioService().sendCode("234324", "COCOCO");
        System.out.println("Hello");
    }
}
