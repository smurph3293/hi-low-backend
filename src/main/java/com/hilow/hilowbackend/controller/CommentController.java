package com.hilow.hilowbackend.controller;

import com.hilow.hilowbackend.exception.ResourceNotFoundException;
import com.hilow.hilowbackend.model.Comment;
import com.hilow.hilowbackend.repository.BetRepository;
import com.hilow.hilowbackend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.validation.Valid;
import java.util.List;

@RestController
@EnableWebMvc
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BetRepository betRepository;

    @GetMapping("/bets/{betId}/comments")
    public List<Comment> getCommentsByBetId(@PathVariable Long betId) {
        return commentRepository.findByBetId(betId);
    }

    @PostMapping("/bets/{betId}/comments")
    public Comment addComment(@PathVariable Long betId,
                            @Valid @RequestBody Comment comment) {
        return betRepository.findById(betId)
                .map(bet -> {
                    comment.setBet(bet);
                    return commentRepository.save(comment);
                }).orElseThrow(() -> new ResourceNotFoundException("Bet not found with id " + betId));
    }

    @PutMapping("/bets/{betId}/comments/{commentId}")
    public Comment updateComment(@PathVariable Long betId,
                               @PathVariable Long commentId,
                               @Valid @RequestBody Comment commentRequest) {
        if(!betRepository.existsById(betId)) {
            throw new ResourceNotFoundException("Bet not found with id " + betId);
        }

        return commentRepository.findById(commentId)
                .map(comment -> {
                    comment.setText(commentRequest.getText());
                    return commentRepository.save(comment);
                }).orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));
    }

    @DeleteMapping("/bets/{betId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long betId,
                                          @PathVariable Long commentId) {
        if(!betRepository.existsById(betId)) {
            throw new ResourceNotFoundException("Bet not found with id " + betId);
        }

        return commentRepository.findById(commentId)
                .map(comment -> {
                    commentRepository.delete(comment);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Comment not found with id " + commentId));

    }
}
