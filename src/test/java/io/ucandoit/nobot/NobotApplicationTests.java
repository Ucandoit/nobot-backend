package io.ucandoit.nobot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
}
