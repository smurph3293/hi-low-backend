package com.amazonaws.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class BetPage {
    private final List<Bet> bets;
    private final String lastEvaluatedKey;
}
