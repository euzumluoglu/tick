package com.challenge.tick.factory;

import com.challenge.tick.data.InstantPriceEntity;
import com.challenge.tick.data.InstantPriceSummaryEntity;
import com.challenge.tick.dto.PriceSummaryDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class PriceAggregatorImpl implements PriceAggregator {

  private ConcurrentHashMap<Integer, InstantPriceEntity> secondsSummaryMap;

  private ReadWriteLock rwLock;

  protected PriceAggregatorImpl() {
    secondsSummaryMap = new ConcurrentHashMap<>(60);
    rwLock = new StampedLock().asReadWriteLock();
  }


  @Override
  public void addNewPrice(LocalDateTime updateTime, Double price) {
    LocalDateTime trimmedUpdateTime = updateTime.withNano(0);
    InstantPriceEntity instantPriceEntity = InstantPriceEntity.builder()
        .now(updateTime)
        .prices(new ArrayList<>()).build();
    instantPriceEntity.getPrices().add(price);

    try {
      rwLock.readLock().lock();
      if (secondsSummaryMap.containsKey(trimmedUpdateTime.getSecond()) || null != secondsSummaryMap.putIfAbsent(trimmedUpdateTime.getSecond(),
          instantPriceEntity)) {
        mergePricesHasTheSameTime(updateTime.getSecond(), instantPriceEntity);
      }
    } finally {
      rwLock.readLock().unlock();
    }

  }

  @Override
  public PriceSummaryDto getStatisticsForCurrentTime() {

    InstantPriceSummaryEntity instantPriceSummary = calculateSummaryAtCurrentTime();
    return instantPriceSummary.getCount() == 0 ?
        initialPriceSummary() : fromInstantToPriceSummaryMap(instantPriceSummary);
  }

  protected InstantPriceSummaryEntity calculateSummaryAtCurrentTime() {

    try {
      rwLock.writeLock().lock();
      return calculateSummaryAtGivenTime(LocalDateTime.now().minusNanos(0));
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  protected PriceSummaryDto initialPriceSummary() {
    return PriceSummaryDto.builder().max(0d).min(0d).count(0l).average(BigDecimal.ZERO).build();
  }

  protected PriceSummaryDto fromInstantToPriceSummaryMap(InstantPriceSummaryEntity instantPriceSummary) {
    return PriceSummaryDto.builder()
        .max(instantPriceSummary.getMax())
        .min(instantPriceSummary.getMin())
        .count(instantPriceSummary.getCount())
        .average(instantPriceSummary.getCount() == 0 ? instantPriceSummary.getTotal() :
            instantPriceSummary.getTotal().divide(BigDecimal.valueOf(instantPriceSummary.getCount()), 2,
                RoundingMode.HALF_UP))
        .build();
  }

  protected InstantPriceSummaryEntity calculateSummaryAtGivenTime(LocalDateTime givenTime) {
    InstantPriceSummaryEntity instantPriceSummary = InstantPriceSummaryEntity
        .builder()
        .max(Double.MIN_VALUE)
        .min(Double.MAX_VALUE)
        .total(BigDecimal.ZERO)
        .count(0)
        .build();

    Collection<InstantPriceEntity> priceInfoValues = secondsSummaryMap.values();
    for (InstantPriceEntity pricesInfo : priceInfoValues) {
      if (!pricesInfo.getNow()
          .isBefore(givenTime.minusSeconds(59))) {
        mergeSummaries(instantPriceSummary, pricesInfo.getPrices());
      }
    }

    if (instantPriceSummary.getCount() == 0) {
      instantPriceSummary.setMin(0);
      instantPriceSummary.setMax(0);
    }
    return instantPriceSummary;
  }

  protected void mergePricesHasTheSameTime(Integer second, InstantPriceEntity instantPriceEntity) {

    secondsSummaryMap.merge(second, instantPriceEntity, (p1, p2) -> {
      if (p1.getNow().isAfter(p2.getNow())) {
        return p1;
      } else if (p1.getNow().isBefore(p2.getNow())) {
        return p2;
      } else {
        p2.getPrices().addAll(p1.getPrices());
        return p2;
      }
    });
  }

  private void mergeSummaries(InstantPriceSummaryEntity summary1, List<Double> prices) {
    summary1.setCount(summary1.getCount() + prices.size());
    for (Double price : prices) {
      summary1.setMax(summary1.getMax() > price ? summary1.getMax() : price);
      summary1.setMin(summary1.getMin() < price ? summary1.getMin() : price);
      summary1.setTotal(summary1.getTotal().add(BigDecimal.valueOf(price)));
    }
  }

}
