package no.hvl.dat250.controllers;

import no.hvl.dat250.dto.PollResult;
import no.hvl.dat250.entities.Poll;
import no.hvl.dat250.entities.Vote;
import no.hvl.dat250.entities.VoteOption;
import no.hvl.dat250.services.PollService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/polls")
public class PollController {

    private final PollService pollService;

    public PollController(PollService pollService) {
        this.pollService = pollService;
    }

    // Poll endpoints

    @GetMapping
    public List<Poll> getAllPolls() {
        return pollService.getAllPolls();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Poll> getPoll(@PathVariable Long id) {
        return pollService.getPoll(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Poll> createPoll(@PathVariable Long userId, @RequestBody Poll poll) {
        return pollService.createPoll(userId, poll)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Poll> updatePoll(@PathVariable Long id, @RequestBody Poll poll) {
        return pollService.updatePoll(id, poll)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoll(@PathVariable Long id) {
        boolean deleted = pollService.deletePoll(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    // Vote Option endpoints

    @GetMapping("/{pollId}/options")
    public ResponseEntity<List<VoteOption>> getOptions(@PathVariable Long pollId) {
        return pollService.getPoll(pollId)
                .map(poll -> ResponseEntity.ok(poll.getOptions()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{pollId}/options")
    public ResponseEntity<VoteOption> addOption(@PathVariable Long pollId, @RequestBody VoteOption option) {
        return pollService.addOption(pollId, option)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{pollId}/options/{optionId}")
    public ResponseEntity<VoteOption> updateOption(@PathVariable Long pollId, @PathVariable Long optionId, @RequestBody VoteOption option) {
        return pollService.getOption(optionId)
                .filter(opt -> opt.getPoll().getId().equals(pollId))
                .flatMap(opt -> pollService.updateOption(optionId, option))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{pollId}/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long pollId, @PathVariable Long optionId) {
        Optional<VoteOption> optionOpt = pollService.getOption(optionId);
        if (optionOpt.isPresent() && optionOpt.get().getPoll().getId().equals(pollId)) {
            boolean deleted = pollService.deleteOption(optionId);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Vote endpoints
    /**
    @GetMapping("/{pollId}/votes")
    public List<Vote> getVotesForPoll(@PathVariable Long pollId) {
        return pollService.getVotesForPoll(pollId);
    }
     **/

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<Vote> castVote(@PathVariable Long pollId, @RequestParam Long userId, @RequestParam Long optionId) {
        return pollService.getOption(optionId)
                .filter(opt -> opt.getPoll().getId().equals(pollId))
                .flatMap(opt -> pollService.castVote(userId, optionId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // Poll results
    @GetMapping("/{pollId}/results")
    public ResponseEntity<List<PollResult>> getPollResults(@PathVariable Long pollId) {
        List<PollResult> results = pollService.getPollResults(pollId);
        return ResponseEntity.ok(results);
    }
}