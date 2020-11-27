package com.challenge.tick.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PriceInfoDto {

  @NotBlank(message = "instrument can not be null or empty")
  private String instrument;

  @JsonFormat(shape = JsonFormat.Shape.STRING)
  @NotNull(message = "price can not be null")
  private Double price;

  @NotNull(message = "timestamp can not be null")
  private Long timestamp;

}
