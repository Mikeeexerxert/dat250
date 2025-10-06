package no.hvl.dat250.services;

import no.hvl.dat250.dto.PollResult;
import no.hvl.dat250.entities.Poll;
import no.hvl.dat250.entities.User;
import no.hvl.dat250.entities.Vote;
import no.hvl.dat250.entities.VoteOption;
import no.hvl.dat250.repositories.PollRepository;
import no.hvl.dat250.repositories.UserRepository;
import no.hvl.dat250.repositories.VoteOptionRepository;
import no.hvl.dat250.repositories.VoteRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PollService {

    private final UserRepository userRepo;
    private final PollRepository pollRepo;
    private final VoteOptionRepository optionRepo;
    private final VoteRepository voteRepo;
    private final PollCacheService cacheService;

    public PollService(UserRepository userRepo,
                       PollRepository pollRepo,
                       VoteOptionRepository optionRepo,
                       VoteRepository voteRepo,
                       PollCacheService cacheService) {
        this.userRepo = userRepo;
        this.pollRepo = pollRepo;
        this.optionRepo = optionRepo;
        this.voteRepo = voteRepo;
        this.cacheService = cacheService;
    }

    // --- Poll CRUD ---

    public List<Poll> getAllPolls() {
        return pollRepo.findAll();
    }

    public Optional<Poll> getPoll(Long id) {
        return pollRepo.findById(id);
    }

    public Optional<Poll> createPoll(Long userId, Poll poll) {
        return userRepo.findById(userId).map(user -> {
            poll.setCreatedBy(user);
            return pollRepo.save(poll);
        });
    }

    public Optional<Poll> updatePoll(Long id, Poll updated) {
        return pollRepo.findById(id).map(existing -> {
            existing.setQuestion(updated.getQuestion());
            existing.setPublishedAt(updated.getPublishedAt());
            existing.setValidUntil(updated.getValidUntil());
            Poll saved = pollRepo.save(existing);
            cacheService.invalidate(id); // invalidate cache on update
            return saved;
        });
    }

    public boolean deletePoll(Long id) {
        if (!pollRepo.existsById(id)) return false;
        pollRepo.deleteById(id);
        cacheService.invalidate(id); // invalidate cache on deletion
        return true;
    }

    // --- VoteOption CRUD ---

    public Optional<VoteOption> getOption(Long optionId) {
        return optionRepo.findById(optionId);
    }

    public Optional<VoteOption> addOption(Long pollId, VoteOption option) {
        return pollRepo.findById(pollId).map(poll -> {
            option.setPoll(poll);
            VoteOption saved = optionRepo.save(option);
            cacheService.invalidate(pollId); // invalidate poll cache
            return saved;
        });
    }

    public Optional<VoteOption> updateOption(Long optionId, VoteOption updated) {
        return optionRepo.findById(optionId).map(existing -> {
            existing.setCaption(updated.getCaption());
            existing.setPresentationOrder(updated.getPresentationOrder());
            VoteOption saved = optionRepo.save(existing);
            cacheService.invalidate(existing.getPoll().getId()); // invalidate poll cache
            return saved;
        });
    }

    public boolean deleteOption(Long optionId) {
        Optional<VoteOption> optionOpt = optionRepo.findById(optionId);
        if (optionOpt.isEmpty()) return false;
        VoteOption option = optionOpt.get();
        Long pollId = option.getPoll().getId();
        optionRepo.deleteById(optionId);
        cacheService.invalidate(pollId); // invalidate poll cache
        return true;
    }

    // --- Vote operations ---

    public Optional<Vote> castVote(Long userId, Long optionId) {
        Optional<User> userOpt = userRepo.findById(userId);
        Optional<VoteOption> optionOpt = optionRepo.findById(optionId);

        if (userOpt.isPresent() && optionOpt.isPresent()) {
            VoteOption option = optionOpt.get();
            Poll poll = option.getPoll();

            // reject if poll expired
            if (poll.getValidUntil() != null && poll.getValidUntil().isBefore(Instant.now())) {
                return Optional.empty();
            }

            Vote vote = new Vote();
            vote.setVoter(userOpt.get());
            vote.setOption(option);
            vote.setPublishedAt(Instant.now());

            Vote saved = voteRepo.save(vote);

            // invalidate cache for this poll
            cacheService.invalidate(poll.getId());

            return Optional.of(saved);
        }

        return Optional.empty();
    }

    public List<Vote> getVotesForPoll(Long pollId) {
        return pollRepo.findById(pollId)
                .map(poll -> poll.getOptions().stream()
                        .flatMap(option -> option.getVotes().stream())
                        .toList())
                .orElse(List.of());
    }

    // --- Poll results with caching ---

    public List<PollResult> getPollResults(Long pollId) {
        if (cacheService.isCached(pollId)) {
            return cacheService.getCachedResults(pollId);
        }

        List<PollResult> results = pollRepo.findById(pollId)
                .map(poll -> poll.getOptions().stream()
                        .map(option -> new PollResult(option.getCaption(),
                                (long) option.getVotes().size()))
                        .toList())
                .orElse(List.of());

        cacheService.cacheResults(pollId, results);
        return results;
    }
}
