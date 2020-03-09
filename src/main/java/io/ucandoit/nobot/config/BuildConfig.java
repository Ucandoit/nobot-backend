package io.ucandoit.nobot.config;

import io.ucandoit.nobot.dto.BuildCost;
import io.ucandoit.nobot.enums.Building;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BuildConfig {

  @Bean
  public Map<Building, Map<Integer, BuildCost>> buildCostMap() {
    log.info("Initialising Build cost map.");
    Map<Building, Map<Integer, BuildCost>> buildCostMap = new HashMap<>();
    buildCostMap.put(Building.STORAGE, getStorageBuildCostMap());
    buildCostMap.put(Building.FOOD, getFoodBuildCostMap());
    buildCostMap.put(Building.PADDY, getPaddyBuildCostMap());
    buildCostMap.put(Building.MARKET, getMarketBuildCostMap());
    buildCostMap.put(Building.HOME_BASIC, getHomeBasicBuildCostMap());
    buildCostMap.put(Building.FIRE, getFireBuildCostMap());
    buildCostMap.put(Building.EARTH, getEarthBuildCostMap());
    buildCostMap.put(Building.WIND, getWindBuildCostMap());
    buildCostMap.put(Building.WATER, getWaterBuildCostMap());
    buildCostMap.put(Building.SKY, getSkyBuildCostMap());
    return buildCostMap;
  }

  private Map<Integer, BuildCost> getStorageBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(8, 0, 0, 4, 1, 24));
    map.put(1, new BuildCost(18, 0, 0, 10, 3, 144));
    map.put(2, new BuildCost(40, 0, 0, 24, 8, 24 * 60));
    map.put(3, new BuildCost(76, 0, 0, 45, 15, 54 * 60));
    map.put(4, new BuildCost(130, 0, 0, 78, 26, 108 * 60));
    map.put(5, new BuildCost(206, 0, 0, 123, 41, 180 * 60));
    map.put(6, new BuildCost(308, 0, 0, 184, 61, 270 * 60));
    map.put(7, new BuildCost(442, 0, 0, 265, 88, 360 * 60));
    map.put(8, new BuildCost(616, 0, 0, 369, 123, 450 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getFoodBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(0, 8, 4, 0, 1, 24));
    map.put(1, new BuildCost(0, 18, 10, 0, 3, 144));
    map.put(2, new BuildCost(0, 40, 24, 0, 8, 24 * 60));
    map.put(3, new BuildCost(0, 76, 45, 0, 15, 54 * 60));
    map.put(4, new BuildCost(0, 130, 78, 0, 26, 108 * 60));
    map.put(5, new BuildCost(0, 206, 123, 0, 41, 180 * 60));
    map.put(6, new BuildCost(0, 308, 184, 0, 61, 270 * 60));
    map.put(7, new BuildCost(0, 442, 265, 0, 88, 360 * 60));
    map.put(8, new BuildCost(0, 616, 369, 0, 123, 450 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getPaddyBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(0, 8, 0, 9, 6, 30));
    map.put(1, new BuildCost(0, 18, 0, 20, 13, 180));
    map.put(2, new BuildCost(0, 37, 0, 43, 27, 14 * 60));
    map.put(3, new BuildCost(0, 70, 0, 80, 50, 48 * 60));
    map.put(4, new BuildCost(0, 119, 0, 136, 85, 108 * 60));
    map.put(5, new BuildCost(0, 187, 0, 214, 133, 171 * 60));
    map.put(6, new BuildCost(0, 278, 0, 318, 199, 342 * 60));
    map.put(7, new BuildCost(0, 395, 0, 452, 282, 570 * 60));
    map.put(8, new BuildCost(0, 542, 0, 620, 387, 798 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getMarketBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(59, 54, 48, 42, 36, 18 * 60));
    map.put(1, new BuildCost(54, 89, 80, 71, 62, 72 * 60));
    map.put(2, new BuildCost(93, 80, 133, 120, 107, 144 * 60));
    map.put(3, new BuildCost(160, 140, 120, 200, 180, 242 * 60));
    map.put(4, new BuildCost(309, 229, 290, 180, 299, 374 * 60));
    map.put(5, new BuildCost(449, 464, 389, 314, 299, 548 * 60));
    map.put(6, new BuildCost(404, 693, 605, 558, 471, 772 * 60));
    map.put(7, new BuildCost(706, 655, 1129, 908, 807, 1054 * 60));
    map.put(8, new BuildCost(1210, 1059, 908, 1513, 1361, 1402 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getHomeBasicBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(0, 0, 0, 0, 0, 5));
    map.put(1, new BuildCost(4, 4, 4, 4, 4, 5));
    map.put(2, new BuildCost(12, 12, 12, 12, 12, 60));
    map.put(3, new BuildCost(22, 22, 22, 22, 22, 4 * 60));
    map.put(4, new BuildCost(37, 37, 37, 37, 37, 16 * 60));
    map.put(5, new BuildCost(60, 60, 60, 60, 60, 48 * 60));
    map.put(6, new BuildCost(94, 94, 94, 94, 94, 144 * 60));
    map.put(7, new BuildCost(142, 142, 142, 142, 142, 288 * 60));
    map.put(8, new BuildCost(207, 207, 207, 207, 207, 504 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getFireBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(14, 6, 0, 0, 0, 30));
    map.put(1, new BuildCost(46, 19, 0, 0, 0, 180));
    map.put(2, new BuildCost(110, 44, 0, 0, 0, 12 * 60));
    map.put(3, new BuildCost(206, 83, 0, 0, 0, 48 * 60));
    map.put(4, new BuildCost(366, 147, 92, 0, 0, 138 * 60));
    map.put(5, new BuildCost(590, 236, 148, 0, 0, 298 * 60));
    map.put(6, new BuildCost(878, 352, 220, 0, 0, 546 * 60));
    map.put(7, new BuildCost(1294, 518, 324, 0, 0, 902 * 60));
    map.put(8, new BuildCost(1838, 736, 460, 0, 0, 1388 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getEarthBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(0, 14, 0, 0, 6, 30));
    map.put(1, new BuildCost(0, 46, 0, 0, 19, 180));
    map.put(2, new BuildCost(0, 110, 0, 0, 44, 12 * 60));
    map.put(3, new BuildCost(0, 206, 0, 0, 83, 48 * 60));
    map.put(4, new BuildCost(92, 366, 0, 0, 147, 138 * 60));
    map.put(5, new BuildCost(148, 590, 0, 0, 236, 298 * 60));
    map.put(6, new BuildCost(220, 878, 0, 0, 352, 546 * 60));
    map.put(7, new BuildCost(324, 1294, 0, 0, 518, 902 * 60));
    map.put(8, new BuildCost(460, 1838, 0, 0, 736, 1388 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getWindBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(6, 0, 14, 0, 0, 30));
    map.put(1, new BuildCost(19, 0, 46, 0, 0, 180));
    map.put(2, new BuildCost(44, 0, 110, 0, 0, 12 * 60));
    map.put(3, new BuildCost(83, 0, 206, 0, 0, 48 * 60));
    map.put(4, new BuildCost(147, 0, 366, 92, 0, 138 * 60));
    map.put(5, new BuildCost(236, 0, 590, 148, 0, 298 * 60));
    map.put(6, new BuildCost(352, 0, 878, 220, 0, 546 * 60));
    map.put(7, new BuildCost(518, 0, 1294, 324, 0, 902 * 60));
    map.put(8, new BuildCost(736, 0, 1838, 460, 0, 1388 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getWaterBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(0, 0, 6, 14, 0, 30));
    map.put(1, new BuildCost(0, 0, 19, 46, 0, 180));
    map.put(2, new BuildCost(0, 0, 44, 110, 0, 12 * 60));
    map.put(3, new BuildCost(0, 0, 83, 206, 0, 48 * 60));
    map.put(4, new BuildCost(0, 0, 147, 366, 92, 138 * 60));
    map.put(5, new BuildCost(0, 0, 236, 590, 148, 298 * 60));
    map.put(6, new BuildCost(0, 0, 352, 878, 220, 546 * 60));
    map.put(7, new BuildCost(0, 0, 518, 1294, 324, 902 * 60));
    map.put(8, new BuildCost(0, 0, 736, 1838, 460, 1388 * 60));
    return map;
  }

  private Map<Integer, BuildCost> getSkyBuildCostMap() {
    Map<Integer, BuildCost> map = new HashMap<>();
    map.put(0, new BuildCost(0, 0, 0, 6, 14, 30));
    map.put(1, new BuildCost(0, 0, 0, 19, 46, 180));
    map.put(2, new BuildCost(0, 0, 0, 44, 110, 12 * 60));
    map.put(3, new BuildCost(0, 0, 0, 83, 206, 48 * 60));
    map.put(4, new BuildCost(0, 92, 0, 147, 366, 138 * 60));
    map.put(5, new BuildCost(0, 148, 0, 236, 590, 298 * 60));
    map.put(6, new BuildCost(0, 220, 0, 352, 878, 546 * 60));
    map.put(7, new BuildCost(0, 324, 0, 518, 1294, 902 * 60));
    map.put(8, new BuildCost(0, 460, 0, 736, 1838, 1388 * 60));
    return map;
  }
}
