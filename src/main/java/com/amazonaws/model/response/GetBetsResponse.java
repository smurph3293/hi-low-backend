package com.amazonaws.model.response;

import com.amazonaws.model.Bet;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonAutoDetect
public class GetBetsResponse {
    private final String lastEvaluatedKey;
    private final List<Bet> bets;
}
