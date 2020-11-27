package com.challenge.tick.data;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InstantPriceEntity {

  private LocalDateTime now;

  private List<Double> prices;
}
