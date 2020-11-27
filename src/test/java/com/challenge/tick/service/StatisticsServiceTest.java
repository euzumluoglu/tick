package com.challenge.tick.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.challenge.tick.dto.PriceSummaryDto;
import com.challenge.tick.factory.PriceAggregator;
import com.challenge.tick.factory.PriceAggregatorFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

  @Mock
  private PriceAggregatorFactory priceAggregatorFactory;

  @Mock
  private PriceAggregator generalPriceAggregator;

  @InjectMocks
  private StatisticsServiceImpl statisticsService;


  @BeforeEach
  void setUp() {

  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(priceAggregatorFactory, generalPriceAggregator);
  }

  @Test
  void timeInValidBehindCurrentTime() {

    LocalDateTime now = LocalDateTime.now();
    Boolean isValid = statisticsService.isRequestTimeValid(now.minusSeconds(60), now);

    assertThat(isValid).isFalse();
  }

  @Test
  void timeInValidAheadCurrentTime() {

    LocalDateTime now = LocalDateTime.now();
    Boolean isValid = statisticsService.isRequestTimeValid(now.plusSeconds(60), now);

    assertThat(isValid).isFalse();
  }

  @Test
  void getPriceForInstrumentWhenNotExist() {
    String key = UUID.randomUUID().toString();
    PriceSummaryDto expectedSummary = PriceSummaryDto.builder()
        .count(0l)
        .average(BigDecimal.ZERO)
        .max(0d)
        .min(0d)
        .build();

    PriceAggregator instrumentPriceAggregator = Mockito.mock(PriceAggregator.class);

    when(priceAggregatorFactory.createNewPriceAggregator()).thenReturn(instrumentPriceAggregator);
    when(instrumentPriceAggregator.getStatisticsForCurrentTime()).thenReturn(expectedSummary);
    PriceSummaryDto summary = statisticsService.getLatestPriceSummaryOfInstrument(key);

    assertThat(summary).isEqualTo(expectedSummary);

    summary = statisticsService.getLatestPriceSummaryOfInstrument(key);
    assertThat(summary).isEqualTo(expectedSummary);

    verify(priceAggregatorFactory, times(1)).createNewPriceAggregator();
    verify(instrumentPriceAggregator, times(2)).getStatisticsForCurrentTime();

  }

  @Test
  void updateGeneralPrice() {
    Double price = 2d;
    LocalDateTime now = LocalDateTime.now();

    statisticsService.updateGeneralPrice(price, now);

    verify(generalPriceAggregator).addNewPrice(eq(now), eq(price));
  }

  @Test
  void updatePriceInstrumentForInstrument() {
    String key = UUID.randomUUID().toString();
    Double price = 2d;
    LocalDateTime now = LocalDateTime.now();

    PriceAggregator instrumentPriceAggregator = Mockito.mock(PriceAggregator.class);

    when(priceAggregatorFactory.createNewPriceAggregator()).thenReturn(instrumentPriceAggregator);

    statisticsService.updateInstrumentPrice(key, price, now);
    statisticsService.updateInstrumentPrice(key, price, now);

    verify(instrumentPriceAggregator, times(2)).addNewPrice(eq(now), eq(price));
    verify(priceAggregatorFactory, times(1)).createNewPriceAggregator();
  }

}
