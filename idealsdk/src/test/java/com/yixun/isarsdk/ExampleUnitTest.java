package com.yixun.isarsdk;

import com.yixun.sdk.util.ISARStringUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test_versionCheck() throws Exception {
        //Log.d("Test", "testVersion");
        String old1 = "2.8";
        String cur1 = "2.9";
        String old2 = "3.1";
        String cur2 = "3.01";
        String old3 = "2.9.1";
        String cur3 = "2.9.2";
        String old4 = "2.9";
        String cur4 = "2.92";
        String old5 = "3.0.1";
        String cur5 = "3.1";

        int t5 = ISARStringUtil.getVersionUpdated(cur1, old1);
        System.out.println(cur1 + "," + old1 + ",result:" + t5);
        assertEquals(1, ISARStringUtil.getVersionUpdated(cur1, old1));
        t5 = ISARStringUtil.getVersionUpdated(old1, cur1);
        System.out.println(old1 + "," + cur1 + ",result:" + t5);
        assertEquals(0, t5);

        t5 = ISARStringUtil.getVersionUpdated(cur2, old2);
        System.out.println(cur2 + "," + old2 + ",result:" + t5);
        assertEquals(0, t5);
        t5 = ISARStringUtil.getVersionUpdated(old2, cur2);
        System.out.println(old2 + "," + cur2 + ",result:" + t5);
        assertEquals(0, t5);

        t5 = ISARStringUtil.getVersionUpdated(cur3, old3);
        System.out.println(cur3 + "," + old3 + ",result:" + t5);
        assertEquals(1, t5);
        t5 = ISARStringUtil.getVersionUpdated(old3, cur3);
        System.out.println(old3 + "," + cur3 + ",result:" + t5);
        assertEquals(0, t5);

        System.out.println("------------cur4");
        t5 = ISARStringUtil.getVersionUpdated(cur4, old4);
        System.out.println(cur4 + "," + old4 + ",result:" + t5);
        assertEquals(1, t5);
        t5 = ISARStringUtil.getVersionUpdated(old4, cur4);
        System.out.println(old4 + "," + cur4 + ",result:" + t5);
        assertEquals(0, t5);

        System.out.println("------------cur5");
        t5 = ISARStringUtil.getVersionUpdated(cur5, old5);
        System.out.println(cur5 + "," + old5 + ",result:" + t5);
        assertEquals(1, t5);
        t5 = ISARStringUtil.getVersionUpdated(old5, cur5);
        System.out.println(old5 + "," + cur5 + ",result:" + t5);
        assertEquals(0, t5);
    }
}