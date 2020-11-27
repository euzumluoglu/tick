package com.challenge.tick.factory;

import org.springframework.stereotype.Service;

@Service
public class PriceAggregatorFactoryImpl implements PriceAggregatorFactory {

  public PriceAggregator createNewPriceAggregator() {
    return new PriceAggregatorImpl();
  }

}
