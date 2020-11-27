package com.challenge.tick.data;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InstantPriceSummaryEntity {

  private BigDecimal total;

  private double min;

  private double max;

  private long count;


}
