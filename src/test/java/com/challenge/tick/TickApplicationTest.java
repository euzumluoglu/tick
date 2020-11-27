package com.challenge.tick;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.tick.dto.PriceInfoDto;
import com.challenge.tick.dto.PriceSummaryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
class TickApplicationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private Map<String, List<Double>> inmemoryPriceInfoMap;

  private Map<String, PriceSummaryDto> restPriceInfoMap;

  private List<String> priceInfosJson;

  private List<String> instrumentNames;

  private Random random = new Random();


  @BeforeEach
  public void setUp() throws JsonProcessingException {

    LocalDateTime now = LocalDateTime.now();
    instrumentNames = new ArrayList<>();
    priceInfosJson = new ArrayList<>();
    inmemoryPriceInfoMap = new HashMap<>();
    restPriceInfoMap = new HashMap<>();
    for (int iId = 0; iId < 10; iId++) {
      String instrumentName = UUID.randomUUID().toString();
      instrumentNames.add(instrumentName);

      List<Double> priceInfos = new ArrayList<>();
      for (int pId = 0; pId < 10; pId++) {
        PriceInfoDto priceInfoDto = buildPriceInfo(instrumentName, now.minusSeconds(random.nextInt(10)));
        priceInfos.add(priceInfoDto.getPrice());
        priceInfosJson.add(objectMapper.writeValueAsString(priceInfoDto));
      }
      inmemoryPriceInfoMap.put(instrumentName, priceInfos);
    }

  }

  @Test
  void tickWithCreatedAndQueryValue() {

    priceInfosJson.stream().forEach(this::tickWithCreatedAndQueryValue);
    instrumentNames.stream().forEach(instrumentName -> restPriceInfoMap.put(instrumentName, this.queryInstrumentPriceSummary(instrumentName)));
    PriceSummaryDto generalPrice = queryGeneralPriceSummary();

    for (String key : restPriceInfoMap.keySet()) {
      List<Double> pricesInMemory = inmemoryPriceInfoMap.get(key);
      PriceSummaryDto summary = restPriceInfoMap.get(key);
      assertThat(summary).isEqualTo(mapToPriceSummary(pricesInMemory));
    }
    assertThat(generalPrice).isEqualTo(aggregatePriceSummaries(restPriceInfoMap.values()));
  }

  PriceSummaryDto aggregatePriceSummaries(Collection<PriceSummaryDto> priceSummaries) {
    Long count = priceSummaries.stream().mapToLong(PriceSummaryDto::getCount).sum();
    BigDecimal total = priceSummaries.stream().map(price -> price.getAverage().multiply(BigDecimal.valueOf(price.getCount())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return PriceSummaryDto.builder()
        .count(count)
        .min(priceSummaries.stream().mapToDouble(PriceSummaryDto::getMin).min().getAsDouble())
        .max(priceSummaries.stream().mapToDouble(PriceSummaryDto::getMax).max().getAsDouble())
        .average(total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP))
        .build();

  }

  PriceSummaryDto mapToPriceSummary(List<Double> prices) {
    BigDecimal total = prices.stream().map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
    return PriceSummaryDto.builder()
        .count((long) prices.size())
        .min(prices.stream().mapToDouble(Double::doubleValue).min().getAsDouble())
        .max(prices.stream().mapToDouble(Double::doubleValue).max().getAsDouble())
        .average(total.divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP))
        .build();
  }


  @SneakyThrows
  PriceSummaryDto queryInstrumentPriceSummary(String instrument) {
    MvcResult mvcResult = this.mockMvc
        .perform(get("/statistics/" + instrument).contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    return (PriceSummaryDto) mvcResult.getAsyncResult();
  }

  @SneakyThrows
  PriceSummaryDto queryGeneralPriceSummary() {
    MvcResult mvcResult = this.mockMvc
        .perform(get("/statistics").contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    return (PriceSummaryDto) mvcResult.getAsyncResult();
  }

  @SneakyThrows
  void tickWithCreatedAndQueryValue(String tickRequest) {

    MvcResult mvcResult = this.mockMvc
        .perform(post("/ticks")
            .content(tickRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andExpect(status().isCreated()).andReturn();

  }

  private PriceInfoDto buildPriceInfo(String instrument, LocalDateTime updateTime) {
    double price = Double.valueOf(random.nextInt(100)) / Double.valueOf(1 + random.nextInt(50));
    return PriceInfoDto.builder()
        .instrument(instrument)
        .price(BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).doubleValue())
        .timestamp(convertToMilliseconds(updateTime))
        .build();
  }

  private long convertToMilliseconds(LocalDateTime updateTime) {
    return updateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
  }

}
