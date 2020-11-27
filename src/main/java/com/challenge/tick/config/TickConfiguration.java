package com.challenge.tick.config;

import com.challenge.tick.factory.PriceAggregator;
import com.challenge.tick.factory.PriceAggregatorFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TickConfiguration {


  /**
   * General Aggregator must be one and should be singleton
   *
   * @param factory
   * @return
   */
  @Bean
  public PriceAggregator generalPriceAggregator(PriceAggregatorFactory factory) {
    return factory.createNewPriceAggregator();
  }

}
