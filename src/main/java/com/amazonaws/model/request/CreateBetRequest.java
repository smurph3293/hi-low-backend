package com.amazonaws.model.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonAutoDetect
public class CreateBetRequest {
    private String customerId;
    private Long preTaxAmount;
    private Long postTaxAmount;
}
