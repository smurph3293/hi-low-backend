package com.amazonaws.model.request;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonAutoDetect
public class BetRequest {
    private String xref; // external reference
    private String creatorXref; // owner of bet
    private List<String> participants; // everyone involved. Used in conditions and punishments
    private String commissionerXref; // optional moderator
    private Date createdAt;
    private String title; // anything
    private String description; // anything
    private String conditions; // you can't jump that, packers win or lose
    private String punishment; // do a video, or cash
    private Date conditionsDeadline; // when a winner must be decided base on conditions
    private Date punishmentDeadline; // when punishment must be completed
    private String resultXref; // url, or video posting
    private List<String> comments; // comments on bet and result
    private Boolean isComplete; // commissioner decided bet punishment is complete or owner
}
