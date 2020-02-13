package com.batm.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilTest {

    @Test
    public void formatPhone() throws Exception {
        assertEquals("+1 801-333-6666", Util.formatPhone("+18013336666"));
        assertEquals("+38 097-919-9703", Util.formatPhone("+380979199703"));
    }
}