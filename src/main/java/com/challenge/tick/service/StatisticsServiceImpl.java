package com.challenge.tick.service;

import com.challenge.tick.dto.PriceInfoDto;
import com.challenge.tick.dto.PriceSummaryDto;
import com.challenge.tick.factory.PriceAggregator;
import com.challenge.tick.factory.PriceAggregatorFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceImpl implements StatisticsService {

  private final ConcurrentHashMap<String, PriceAggregator> pricesSummaryForInstruments;
  private final PriceAggregator generalPriceAggregator;
  private final PriceAggregatorFactory priceAggregatorFactory;

  @Autowired
  public StatisticsServiceImpl(PriceAggregatorFactory priceAggregatorFactory, PriceAggregator generalPriceAggregator) {
    pricesSummaryForInstruments = new ConcurrentHashMap<>();
    this.priceAggregatorFactory = priceAggregatorFactory;
    this.generalPriceAggregator = generalPriceAggregator;
  }

  public PriceSummaryDto getLatestPriceSummaryOfInstrument(String instrumentKey) {
    PriceAggregator instrumentAggregator = getPriceAggregatorForInstrument(instrumentKey);
    return instrumentAggregator.getStatisticsForCurrentTime();
  }


  public PriceSummaryDto getLatestGeneralPriceSummary() {
    return generalPriceAggregator.getStatisticsForCurrentTime();
  }

  @Override
  public boolean processIncomingMessage(PriceInfoDto priceInfoDto) {
    LocalDateTime requestTime =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(priceInfoDto.getTimestamp()), ZoneId.systemDefault()).withNano(0);

    if (isRequestTimeValid(requestTime, LocalDateTime.now().withNano(0))) {
      updateInstrumentPrice(priceInfoDto.getInstrument(), priceInfoDto.getPrice(), requestTime);
      updateGeneralPrice(priceInfoDto.getPrice(), requestTime);
      return true;
    }
    return false;
  }

  protected boolean isRequestTimeValid(LocalDateTime requestTime, LocalDateTime now) {

    long differenceInSeconds = Duration.between(requestTime, now).getSeconds();
    return differenceInSeconds < 60 && differenceInSeconds >= 0;
  }

  protected void updateGeneralPrice(Double newPrice, LocalDateTime timestamp) {
    generalPriceAggregator.addNewPrice(timestamp, newPrice);
  }


  protected void updateInstrumentPrice(String instrumentKey, Double newPrice, LocalDateTime timestamp) {
    PriceAggregator instrumentAggregator = getPriceAggregatorForInstrument(instrumentKey);
    instrumentAggregator.addNewPrice(timestamp, newPrice);
  }

  protected PriceAggregator getPriceAggregatorForInstrument(String instrumentKey) {
    PriceAggregator instrumentAggregator = pricesSummaryForInstruments.get(instrumentKey);
    if (instrumentAggregator == null) {
      instrumentAggregator = priceAggregatorFactory.createNewPriceAggregator();
      PriceAggregator prevPriceAggregator = pricesSummaryForInstruments.putIfAbsent(instrumentKey, instrumentAggregator);
      return prevPriceAggregator == null ? instrumentAggregator : prevPriceAggregator;
    }
    return instrumentAggregator;
  }

}
