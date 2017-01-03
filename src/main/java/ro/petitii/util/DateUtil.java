package ro.petitii.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static Date deadline(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE,30);
        return cal.getTime();
    }
}
