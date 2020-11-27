package com.challenge.tick.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
@JsonInclude(Include.NON_NULL)
public class PriceSummaryDto {

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BigDecimal average;

  private Double min;

  private Double max;

  private Long count;
}
