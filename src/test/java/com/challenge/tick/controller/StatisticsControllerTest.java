package com.challenge.tick.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.tick.dto.PriceSummaryDto;
import com.challenge.tick.service.StatisticsService;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@WebMvcTest(controllers = StatisticsController.class)
class StatisticsControllerTest {

  @MockBean
  private StatisticsService statisticsService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void getGeneralPriceSummary() throws Exception {

    PriceSummaryDto priceSummaryDto = PriceSummaryDto.builder()
        .count(5l)
        .average(BigDecimal.TEN)
        .max(10d)
        .min(2d)
        .build();
    when(statisticsService.getLatestGeneralPriceSummary()).thenReturn(priceSummaryDto);

    MvcResult mvcResult = this.mockMvc
        .perform(get("/statistics").contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.count").value(priceSummaryDto.getCount()))
        .andExpect(jsonPath("$.min").value(priceSummaryDto.getMin()))
        .andExpect(jsonPath("$.max").value(priceSummaryDto.getMax()))
        .andExpect(jsonPath("$.average").value(priceSummaryDto.getAverage()));

    verify(statisticsService).getLatestGeneralPriceSummary();
  }

  @Test
  void getPriceSummaryForAnInstrument() throws Exception {

    PriceSummaryDto priceSummaryDto = PriceSummaryDto.builder()
        .count(5l)
        .average(BigDecimal.TEN)
        .max(10d)
        .min(2d)
        .build();

    String instrument = UUID.randomUUID().toString();

    when(statisticsService.getLatestPriceSummaryOfInstrument(eq(instrument))).thenReturn(priceSummaryDto);

    MvcResult mvcResult = this.mockMvc
        .perform(get("/statistics/" + instrument).contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.count").value(priceSummaryDto.getCount()))
        .andExpect(jsonPath("$.min").value(priceSummaryDto.getMin()))
        .andExpect(jsonPath("$.max").value(priceSummaryDto.getMax()))
        .andExpect(jsonPath("$.average").value(priceSummaryDto.getAverage())).andReturn();

    verify(statisticsService).getLatestPriceSummaryOfInstrument(eq(instrument));
  }

  @Test
  void getPriceSummaryForAnInstrumentWhileExceptionOccured() throws Exception {

    String instrument = UUID.randomUUID().toString();
    String errorMessage = "Something went wrong";

    when(statisticsService.getLatestPriceSummaryOfInstrument(anyString())).thenThrow(new IllegalMonitorStateException(errorMessage));

    MvcResult mvcResult = this.mockMvc
        .perform(get("/statistics/" + instrument).contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andDo(print())
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.error").exists())
        .andExpect(jsonPath("$.details[0]").value(errorMessage));

    verify(statisticsService).getLatestPriceSummaryOfInstrument(eq(instrument));

  }

}
