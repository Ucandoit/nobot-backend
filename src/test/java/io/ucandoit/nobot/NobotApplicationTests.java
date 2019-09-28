package io.ucandoit.nobot;

import io.ucandoit.nobot.enums.WarStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NobotApplicationTests {

  @Test
  public void contextLoads() {
    int delay = calculateInitialDelay(0);
    System.out.println(delay);
  }

  @Test
  public void checkLastDayTests() throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    Date date1 = dateFormat.parse("2019-09-30 07:01:00");
    Assert.assertEquals(WarStatus.START, checkLastDay(date1));
    Date date2 = dateFormat.parse("2019-10-03 07:01:00");
    Assert.assertEquals(WarStatus.LAST_DAY, checkLastDay(date2));
    Date date3 = dateFormat.parse("2019-10-04 00:01:00");
    Assert.assertEquals(WarStatus.LAST_DAY, checkLastDay(date3));
    Date date4 = dateFormat.parse("2019-10-04 07:01:00");
    Assert.assertEquals(WarStatus.STOP, checkLastDay(date4));
  }

  private int calculateInitialDelay(int startHour) {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(new Date());
    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
    Calendar targetCalendar = GregorianCalendar.getInstance();
    targetCalendar.setTime(calendar.getTime());
    if (currentHour >= startHour) {
      if (currentHour < startHour + 3) {
        return 0;
      } else {
        targetCalendar.add(Calendar.DATE, 1);
      }
    }
    targetCalendar.set(Calendar.HOUR_OF_DAY, startHour);
    targetCalendar.set(Calendar.MINUTE, 0);
    targetCalendar.set(Calendar.SECOND, 30);
    targetCalendar.set(Calendar.MILLISECOND, 0);
    long diff = targetCalendar.getTime().getTime() - new Date().getTime();
    return (int) (diff / 1000);
  }

  private WarStatus checkLastDay(Date date) {
    try {
      Date lastDay = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2019-10-03 07:00:00");
      if (lastDay.after(date)) {
        return WarStatus.START;
      } else {
        if (date.getTime() - lastDay.getTime() < 24 * 60 * 60 * 1000) {
          return WarStatus.LAST_DAY;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return WarStatus.STOP;
  }
}
