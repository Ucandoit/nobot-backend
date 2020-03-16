package io.ucandoit.nobot.config;

import io.ucandoit.nobot.dto.ResourceCost;
import io.ucandoit.nobot.enums.Training;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class TrainingConfig {

  @Bean
  public Map<Training, Map<Integer, ResourceCost>> trainingCostMap() {
    log.info("Initialising Training cost map.");
    Map<Training, Map<Integer, ResourceCost>> trainingCostMap = new HashMap<>();
    for (Training training : Training.values()) {
      trainingCostMap.put(training, getTrainCostMap(training));
    }
    return trainingCostMap;
  }

  private Map<Integer, ResourceCost> getTrainCostMap(Training training) {
    Map<Integer, ResourceCost> map = new HashMap<>();
    map.put(0, toResourceCost(training, 8, 2, 0, 60, 10));
    map.put(1, toResourceCost(training, 13, 3, 0, 120, 15));
    map.put(2, toResourceCost(training, 23, 5, 0, 210, 20));
    map.put(3, toResourceCost(training, 38, 9, 0, 330, 25));
    map.put(4, toResourceCost(training, 60, 15, 0, 750, 50));
    map.put(5, toResourceCost(training, 89, 22, 0, 23 * 60, 80));
    map.put(6, toResourceCost(training, 125, 31, 0, 38 * 60 + 30, 120));
    map.put(7, toResourceCost(training, 168, 42, 0, 58 * 60 + 30, 180));
    map.put(8, toResourceCost(training, 218, 54, 0, 83 * 60, 300));
    map.put(9, toResourceCost(training, 275, 68, 0, 112 * 60, 480));
    map.put(10, toResourceCost(training, 342, 85, 34, 154 * 60, 720));
    map.put(11, toResourceCost(training, 419, 104, 41, 210 * 60 + 30, 1050));
    map.put(12, toResourceCost(training, 506, 126, 50, 280 * 60 + 30, 1470));
    map.put(13, toResourceCost(training, 603, 150, 60, 363 * 60, 1980));
    map.put(14, toResourceCost(training, 710, 177, 71, 459 * 60 + 30, 2580));
    map.put(15, toResourceCost(training, 827, 206, 82, 569 * 60 + 30, 3270));
    map.put(16, toResourceCost(training, 959, 239, 95, 720 * 60, 4050));
    map.put(17, toResourceCost(training, 1106, 276, 110, 911 * 60, 4920));
    map.put(18, toResourceCost(training, 1268, 317, 126, 1142 * 60 + 30, 5880));
    map.put(19, toResourceCost(training, 1445, 361, 144, 1414 * 60 + 30, 6960));
    return map;
  }

  private ResourceCost toResourceCost(
      Training training, int first, int second, int third, int seconds) {
    ResourceCost resourceCost = new ResourceCost();
    resourceCost.setSeconds(seconds);
    switch (training) {
      case FIRE:
        resourceCost.setFire(first);
        resourceCost.setSky(second);
        resourceCost.setEarth(third);
        break;
      case EARTH:
        resourceCost.setEarth(first);
        resourceCost.setFire(second);
        resourceCost.setWind(third);
        break;
      case WIND:
        resourceCost.setWind(first);
        resourceCost.setEarth(second);
        resourceCost.setWater(third);
        break;
      case WATER:
        resourceCost.setWater(first);
        resourceCost.setWind(second);
        resourceCost.setSky(third);
        break;
      case SKY:
        resourceCost.setSky(first);
        resourceCost.setWater(second);
        resourceCost.setFire(third);
        break;
      default:
        break;
    }
    return resourceCost;
  }

  private ResourceCost toResourceCost(
      Training training, int first, int second, int third, int seconds, int reducedSeconds) {
    ResourceCost resourceCost = toResourceCost(training, first, second, third, seconds);
    resourceCost.setReducedSeconds(reducedSeconds);
    return resourceCost;
  }
}
