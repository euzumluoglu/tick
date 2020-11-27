package com.challenge.tick.controller;

import com.challenge.tick.dto.PriceSummaryDto;
import com.challenge.tick.service.StatisticsService;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

  private final StatisticsService statisticsService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  public CompletableFuture<PriceSummaryDto> getGeneralStatistics() {

    return CompletableFuture.supplyAsync(statisticsService::getLatestGeneralPriceSummary);
  }

  @GetMapping(value = "/{instrument_identifier}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.OK)
  public CompletableFuture<PriceSummaryDto> getStatisticsByInstrumentId(@PathVariable("instrument_identifier") String instrumentId) {
    return CompletableFuture.supplyAsync(() -> statisticsService.getLatestPriceSummaryOfInstrument(instrumentId));
  }

}
