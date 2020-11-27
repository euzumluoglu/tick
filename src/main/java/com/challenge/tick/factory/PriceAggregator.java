package com.challenge.tick.factory;

import com.challenge.tick.dto.PriceSummaryDto;
import java.time.LocalDateTime;

public interface PriceAggregator {

  void addNewPrice(LocalDateTime newUpdateTime, Double val);

  PriceSummaryDto getStatisticsForCurrentTime();

}
