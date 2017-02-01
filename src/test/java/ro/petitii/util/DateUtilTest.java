package ro.petitii.util;

import org.junit.Test;
import ro.petitii.config.DeadlineConfig;
import ro.petitii.model.PetitionStatus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static ro.petitii.util.DateUtil.alertStatus;

public class DateUtilTest {
    private static final DateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    private DeadlineConfig config;

    public DateUtilTest() {
        config = new DeadlineConfig();
        config.setDays(30);
        config.setRedAlert(0.9f);
        config.setYellowAlert(0.75f);
    }

    @Test
    public void testRed() throws ParseException {
        Date current = df.parse("14.01.2017");
        Date deadline = df.parse("17.01.2017");
        assertEquals("red", alertStatus(PetitionStatus.Status.IN_PROGRESS, current, deadline, config));
    }

    @Test
    public void testYellow() throws ParseException {
        Date current = df.parse("14.01.2017");
        Date deadline = df.parse("18.01.2017");
        assertEquals("yellow", alertStatus(PetitionStatus.Status.IN_PROGRESS, current, deadline, config));

        current = df.parse("14.01.2017");
        deadline = df.parse("22.01.2017");
        assertEquals("yellow", alertStatus(PetitionStatus.Status.IN_PROGRESS, current, deadline, config));
    }

    @Test
    public void testNone() throws ParseException {
        Date current = df.parse("14.01.2017");
        Date deadline = df.parse("23.01.2017");
        assertEquals("", alertStatus(PetitionStatus.Status.IN_PROGRESS, current, deadline, config));
    }

    @Test
    public void testOverdue() throws ParseException {
        Date current = df.parse("14.01.2017");
        Date deadline = df.parse("10.01.2017");
        assertEquals("red", alertStatus(PetitionStatus.Status.IN_PROGRESS, current, deadline, config));
    }

    @Test
    public void testGreen() throws ParseException {
        Date current = df.parse("14.01.2017");
        Date deadline = df.parse("10.01.2017");
        assertEquals("green", alertStatus(PetitionStatus.Status.SOLVED, current, deadline, config));
        assertEquals("green", alertStatus(PetitionStatus.Status.CLOSED, current, deadline, config));
    }
}
