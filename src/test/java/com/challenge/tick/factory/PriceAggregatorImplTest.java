package com.challenge.tick.factory;

import com.challenge.tick.data.InstantPriceSummaryEntity;
import com.challenge.tick.dto.PriceSummaryDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PriceAggregatorImplTest {

  private PriceAggregatorImpl priceAggregator;
  private Map<String, InstantPriceSummaryEntity> pricesListInSecondsMap;

  @BeforeEach
  void setUp() {
    priceAggregator = new PriceAggregatorImpl();
    pricesListInSecondsMap = new HashMap<>();
  }

  @Test
  void addPriceInfoSuccessfully() {
    LocalDateTime now = LocalDateTime.now();
    SimpleEntry[] secondsPrices = {new SimpleEntry(0, 8d), new SimpleEntry(2, 5d), new SimpleEntry(4, 2d), new SimpleEntry(5, 7d)
        , new SimpleEntry(44, 1d), new SimpleEntry(34, 4d), new SimpleEntry(44, 9d), new SimpleEntry(104, 3d), new SimpleEntry(105, 7d)
        , new SimpleEntry(107, 16d), new SimpleEntry(105, 20d), new SimpleEntry(164, 11d), new SimpleEntry(165, 2d)};

    LocalDateTime latestUpdateTime = now;
    for (int i = 0; i < secondsPrices.length; i++) {
      LocalDateTime expectedTime = now.plusSeconds((int) secondsPrices[i].getKey()).withNano(0);
      Double price = (double) secondsPrices[i].getValue();
      priceAggregator.addNewPrice(expectedTime, price);
      addNewPriceInfoToTest(expectedTime, price);
      /**
       * this control for the prices which has old dates
       * comparison must be done starting from the closest one to the current time
       */
      if (latestUpdateTime.isBefore(now.plusSeconds((int) secondsPrices[i].getKey()))) {
        latestUpdateTime = now.plusSeconds((int) secondsPrices[i].getKey());
      }
      for (int j = 0; j <= 60; j++) {
        InstantPriceSummaryEntity instantPriceSummaryEntity = priceAggregator.calculateSummaryAtGivenTime(latestUpdateTime.plusSeconds(j).withNano(0));
        PriceSummaryDto summary = mapInstantToPriceSummary(instantPriceSummaryEntity);
        PriceSummaryDto inMemoryPriceSummaryDto = getPriceSummaryInTest(latestUpdateTime.plusSeconds(j));
        //System.out.println((i*60 + j)+" second"+i * 60+" "+j +"  " +inMemoryPriceSummary);
        Assertions.assertThat(summary).isEqualTo(inMemoryPriceSummaryDto);
      }
    }

  }

  private PriceSummaryDto mapInstantToPriceSummary(InstantPriceSummaryEntity instantPriceSummaryEntity) {
    return PriceSummaryDto.builder()
        .count(instantPriceSummaryEntity.getCount())
        .average(instantPriceSummaryEntity.getTotal().equals(BigDecimal.ZERO) ? BigDecimal.ZERO
            : instantPriceSummaryEntity.getTotal().divide(BigDecimal.valueOf(instantPriceSummaryEntity.getCount()), 2, RoundingMode.HALF_UP))
        .min(instantPriceSummaryEntity.getMin())
        .max(instantPriceSummaryEntity.getMax())
        .build();
  }

  @Test
  void addPriceInfoWithMultipleThread() throws InterruptedException {
    int startSecond = 0;
    int endSecond = 10;
    LocalDateTime updateTime = LocalDateTime.now().withNano(0);
    List<Callable<String>> runnableList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      SimpleEntry[] priceValuePairs = createRandomPriceValuePairs(startSecond, endSecond, updateTime);
      mapPairsToInMemory(priceValuePairs);
      runnableList.add(prepareRunnable(priceAggregator, priceValuePairs));
    }

    ExecutorService executor = Executors.newFixedThreadPool(10);

    executor.invokeAll(runnableList);

    executor.awaitTermination(5, TimeUnit.SECONDS);

    InstantPriceSummaryEntity instantPriceSummaryEntity = priceAggregator.calculateSummaryAtGivenTime(updateTime.plusSeconds(10));
    PriceSummaryDto inMemoryPriceSummaryDto = getPriceSummaryInTest(updateTime.plusSeconds(10));

    PriceSummaryDto summary = mapInstantToPriceSummary(instantPriceSummaryEntity);

    Assertions.assertThat(summary).isEqualTo(inMemoryPriceSummaryDto);

  }

  private void mapPairsToInMemory(SimpleEntry[] secondsPrices) {
    for (SimpleEntry<LocalDateTime, Double> entry : secondsPrices) {
      addNewPriceInfoToTest(entry.getKey(), entry.getValue());
    }

  }

  private Callable prepareRunnable(PriceAggregatorImpl priceAggregator, SimpleEntry[] secondsPrices) {
    Callable runnableTask = () -> {
      for (SimpleEntry<LocalDateTime, Double> entry : secondsPrices) {
        priceAggregator.addNewPrice(entry.getKey(), entry.getValue());
      }
      return "";
    };
    return runnableTask;
  }

  private SimpleEntry[] createRandomPriceValuePairs(int startSecond, int endsecond, LocalDateTime updateTime) {
    Random random = new Random();
    SimpleEntry[] secondsPrice = new SimpleEntry[endsecond - startSecond + 1];
    for (int i = 0; i < secondsPrice.length; i++) {
      double price = Double.valueOf(random.nextInt(100)) / Double.valueOf(1 + random.nextInt(50));
      secondsPrice[i] = new SimpleEntry(updateTime.plusSeconds(i), BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).doubleValue());
    }
    return secondsPrice;
  }


  private PriceSummaryDto getPriceSummaryInTest(LocalDateTime time) {

    InstantPriceSummaryEntity instantPriceSummaryEntity = pricesListInSecondsMap.get(time.withNano(0).toString());
    if (instantPriceSummaryEntity == null) {
      return PriceSummaryDto.builder().count(0l).max(0d).min(0d).average(BigDecimal.ZERO).build();
    }
    return PriceSummaryDto.builder()
        .count(instantPriceSummaryEntity.getCount())
        .max(instantPriceSummaryEntity.getMax())
        .min(instantPriceSummaryEntity.getMin())
        .average(instantPriceSummaryEntity.getTotal().divide(BigDecimal.valueOf(instantPriceSummaryEntity.getCount()), 2, RoundingMode.HALF_UP))
        .build();
  }

  private void addNewPriceInfoToTest(LocalDateTime newUpdate, double val) {

    for (int i = 0; i < 60; i++) {
      String timestamp = newUpdate.withNano(0).plusSeconds(i).toString();
      InstantPriceSummaryEntity summary = pricesListInSecondsMap.get(timestamp);
      if (summary == null) {
        summary = InstantPriceSummaryEntity
            .builder()
            .count(1)
            .max(val)
            .min(val)
            .total(BigDecimal.valueOf(val)).build();
        pricesListInSecondsMap.put(timestamp, summary);
      } else {
        summary.setCount(summary.getCount() + 1);
        summary.setMax(summary.getMax() < val ? val : summary.getMax());
        summary.setMin(summary.getMin() > val ? val : summary.getMin());
        summary.setTotal(summary.getTotal().add(BigDecimal.valueOf(val)));

      }
    }
  }
}
