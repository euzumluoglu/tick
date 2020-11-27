package com.challenge.tick.service;

import com.challenge.tick.dto.PriceInfoDto;
import com.challenge.tick.dto.PriceSummaryDto;

public interface StatisticsService {


  PriceSummaryDto getLatestPriceSummaryOfInstrument(String instrumentKey);

  PriceSummaryDto getLatestGeneralPriceSummary();

  boolean processIncomingMessage(PriceInfoDto priceInfoDto);

}
