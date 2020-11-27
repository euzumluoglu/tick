package com.challenge.tick.controller;


import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.challenge.tick.dto.PriceInfoDto;
import com.challenge.tick.service.StatisticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


@WebMvcTest(controllers = TicksController.class)
class TicksControllerTest {

  @MockBean
  private StatisticsService statisticsService;


  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private String tickRequest;

  private String tickRequestWithMissingField;

  private PriceInfoDto priceInfoDto;

  private PriceInfoDto priceInfoDtoWithMissingField;


  @BeforeEach
  public void setUp() throws JsonProcessingException {
    priceInfoDto = PriceInfoDto.builder()
        .instrument(UUID.randomUUID().toString())
        .price(2d)
        .timestamp(System.currentTimeMillis())
        .build();

    tickRequest = objectMapper.writeValueAsString(priceInfoDto);

    priceInfoDtoWithMissingField = PriceInfoDto.builder()
        .instrument(UUID.randomUUID().toString())
        .timestamp(System.currentTimeMillis())
        .build();

    tickRequestWithMissingField = objectMapper.writeValueAsString(priceInfoDtoWithMissingField);

  }


  @Test
  void tickWithCreatedResponse() throws Exception {

    when(statisticsService.processIncomingMessage(eq(priceInfoDto))).thenReturn(Boolean.TRUE);

    MvcResult mvcResult = this.mockMvc
        .perform(post("/ticks")
            .content(tickRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andDo(print())
        .andExpect(status().isCreated());

    verify(statisticsService).processIncomingMessage(eq(priceInfoDto));
  }

  @Test
  void tickWithNoContentResponse() throws Exception {

    when(statisticsService.processIncomingMessage(eq(priceInfoDto))).thenReturn(Boolean.FALSE);

    MvcResult mvcResult = this.mockMvc
        .perform(post("/ticks")
            .content(tickRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(request().asyncStarted())
        .andExpect(status().isOk())
        .andReturn();

    this.mockMvc.perform(asyncDispatch(mvcResult))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(statisticsService).processIncomingMessage(eq(priceInfoDto));
  }

  @Test
  void tickWithMissingFields() throws Exception {

    this.mockMvc
        .perform(post("/ticks")
            .content(tickRequestWithMissingField)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
        .andExpect(request().asyncNotStarted())
        .andExpect(status().is4xxClientError())
        .andReturn();

  }

}
