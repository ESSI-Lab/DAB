package eu.essi_lab.accessor.smhi;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class TimeTest {

    @Test
    public void test() {
	Date date = new Date(441756000000l);
	System.out.println(date);
	date = new Date(1725141600000l);
	System.out.println(date);
    }

}
