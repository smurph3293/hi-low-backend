package com.hilow.hilowbackend.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bets")
public class Bet extends AuditModel {
    @Id
    @GeneratedValue(generator = "bet_generator")
    @SequenceGenerator(
            name = "bet_generator",
            sequenceName = "bet_sequence",
            initialValue = 1000
    )
    private Long id;

    @Column(nullable = false)
    private String xref = UUID.randomUUID().toString(); // external reference

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // owner of bet

    @NotBlank
    @Size(min = 3, max = 100)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToMany
    @JoinTable(
            name = "bet_participants",
            joinColumns = @JoinColumn(name = "bet_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    Set<User> participants; // everyone involved. Used in conditions and punishments


    /*
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commissioner_id", nullable = false)
    private User commissionerXref; // optional moderator
     */

    @Column(columnDefinition = "text")
    private String conditions; // you can't jump that, packers win or lose

    @Column(columnDefinition = "text")
    private String punishment; // do a video, or cash

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "conditions_deadline", nullable = false)
    private Date conditionsDeadline; // when a winner must be decided base on conditions

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "punishment_deadline", nullable = false)
    private Date punishmentDeadline; // when punishment must be completed

    @Column(columnDefinition = "text")
    private String resultXref; // url, or video posting

    @OneToMany(mappedBy = "bet")
    private List<Comment> comments = new ArrayList<>();

    @Column
    private Boolean isComplete; // commissioner decided bet punishment is complete or owner
}
