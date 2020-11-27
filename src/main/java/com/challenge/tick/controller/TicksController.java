package com.challenge.tick.controller;

import com.challenge.tick.dto.PriceInfoDto;
import com.challenge.tick.service.StatisticsService;
import java.util.concurrent.CompletableFuture;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ticks")
@RequiredArgsConstructor
public class TicksController {

  private final StatisticsService statisticsService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public CompletableFuture<ResponseEntity<Void>> processTick(@Valid @RequestBody PriceInfoDto priceInfoDto) {

    return CompletableFuture
        .supplyAsync(() -> statisticsService.processIncomingMessage(priceInfoDto))
        .thenApply(isContent -> isContent ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.noContent().build());
  }

}
