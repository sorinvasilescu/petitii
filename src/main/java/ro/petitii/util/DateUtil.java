package ro.petitii.util;

import ro.petitii.config.DeadlineConfig;
import ro.petitii.model.PetitionStatus;

import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    /**
     * @param d        - the date to be shifted
     * @param deadline - in days
     * @return a new Date = d + deadline
     */

    public static Date deadline(Date d, int deadline) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, deadline);
        return cal.getTime();
    }

    public static String alertStatus(PetitionStatus.Status status, Date currentDate, Date deadline, DeadlineConfig deadlineConfig) {
        if (status == PetitionStatus.Status.CLOSED || status == PetitionStatus.Status.SOLVED) {
            return "green";
        } else {
            long days = days(currentDate, deadline);
            if (days <= deadlineConfig.redAlert()) {
                return "red";
            }

            if (days <= deadlineConfig.yellowAlert()) {
                return "yellow";
            }

            return "";
        }
    }

    private static long days(Date d1, Date d2) {
        return ChronoUnit.DAYS.between(d1.toInstant(), d2.toInstant());
    }
}
