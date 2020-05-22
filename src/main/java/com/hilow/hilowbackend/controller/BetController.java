package com.hilow.hilowbackend.controller;

import com.hilow.hilowbackend.exception.ResourceNotFoundException;
import com.hilow.hilowbackend.model.Bet;
import com.hilow.hilowbackend.repository.BetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.validation.Valid;

@RestController
@EnableWebMvc
public class BetController {

    @Autowired
    private BetRepository betRepository;

    @GetMapping("/bets")
    public Page<Bet> getBets(Pageable pageable) {
        return betRepository.findAll(pageable);
    }


    @PostMapping("/bets")
    public Bet createBet(@Valid @RequestBody Bet bet) {
        return betRepository.save(bet);
    }

    @PutMapping("/bets/{betId}")
    public Bet updateBet(@PathVariable Long betId,
                                   @Valid @RequestBody Bet betRequest) {
        return betRepository.findById(betId)
                .map(bet -> {
                    bet.setTitle(betRequest.getTitle());
                    bet.setDescription(betRequest.getDescription());
                    return betRepository.save(bet);
                }).orElseThrow(() -> new ResourceNotFoundException("Bet not found with id " + betId));
    }


    @DeleteMapping("/bets/{betId}")
    public ResponseEntity<?> deleteBet(@PathVariable Long betId) {
        return betRepository.findById(betId)
                .map(bet -> {
                    betRepository.delete(bet);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Bet not found with id " + betId));
    }
}
