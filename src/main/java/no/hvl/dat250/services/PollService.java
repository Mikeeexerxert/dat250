package no.hvl.dat250.services;

import no.hvl.dat250.dto.PollResult;
import no.hvl.dat250.entities.Poll;
import no.hvl.dat250.entities.User;
import no.hvl.dat250.entities.Vote;
import no.hvl.dat250.entities.VoteOption;
import no.hvl.dat250.dto.VoteEvent;
import no.hvl.dat250.repositories.PollRepository;
import no.hvl.dat250.repositories.UserRepository;
import no.hvl.dat250.repositories.VoteOptionRepository;
import no.hvl.dat250.repositories.VoteRepository;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    // Messaging dependencies
    private final AmqpAdmin amqpAdmin;
    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange pollExchange;

    public PollService(UserRepository userRepo,
                       PollRepository pollRepo,
                       VoteOptionRepository optionRepo,
                       VoteRepository voteRepo,
                       PollCacheService cacheService,
                       AmqpAdmin amqpAdmin,
                       RabbitTemplate rabbitTemplate,
                       TopicExchange pollExchange) {
        this.userRepo = userRepo;
        this.pollRepo = pollRepo;
        this.optionRepo = optionRepo;
        this.voteRepo = voteRepo;
        this.cacheService = cacheService;
        this.amqpAdmin = amqpAdmin;
        this.rabbitTemplate = rabbitTemplate;
        this.pollExchange = pollExchange;
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
            Poll saved = pollRepo.save(poll);

            // Register topic (exchange binding) for this poll
            String routingKey = "poll." + saved.getId();
            Queue queue = new Queue("poll." + saved.getId() + ".queue", true);
            amqpAdmin.declareQueue(queue);
            amqpAdmin.declareBinding(
                    BindingBuilder.bind(queue).to(pollExchange).with(routingKey)
            );

            System.out.println("📡 Created RabbitMQ topic for poll: " + routingKey);

            return saved;
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
            cacheService.invalidate(pollId);
            return saved;
        });
    }

    public Optional<VoteOption> updateOption(Long optionId, VoteOption updated) {
        return optionRepo.findById(optionId).map(existing -> {
            existing.setCaption(updated.getCaption());
            existing.setPresentationOrder(updated.getPresentationOrder());
            VoteOption saved = optionRepo.save(existing);
            cacheService.invalidate(existing.getPoll().getId());
            return saved;
        });
    }

    public boolean deleteOption(Long optionId) {
        Optional<VoteOption> optionOpt = optionRepo.findById(optionId);
        if (optionOpt.isEmpty()) return false;
        VoteOption option = optionOpt.get();
        Long pollId = option.getPoll().getId();
        optionRepo.deleteById(optionId);
        cacheService.invalidate(pollId);
        return true;
    }

    // --- Vote operations ---

    public Optional<Vote> castVote(Long userId, Long optionId) {
        Optional<User> userOpt = userRepo.findById(userId);
        Optional<VoteOption> optionOpt = optionRepo.findById(optionId);

        if (optionOpt.isEmpty()) return Optional.empty();
        VoteOption option = optionOpt.get();
        Poll poll = option.getPoll();

        // reject if poll expired
        if (poll.getValidUntil() != null && poll.getValidUntil().isBefore(Instant.now())) {
            return Optional.empty();
        }

        // Create new vote
        Vote vote = new Vote();
        vote.setOption(option);
        vote.setPublishedAt(Instant.now());
        userOpt.ifPresent(vote::setVoter);

        Vote saved = voteRepo.save(vote);

        // Invalidate cache for this poll
        cacheService.invalidate(poll.getId());

        // Publish vote event to RabbitMQ
        publishVoteEvent(poll.getId(), option.getId(), userOpt.map(User::getId).orElse(null));

        return Optional.of(saved);
    }

    // --- Messaging ---

    private void publishVoteEvent(Long pollId, Long optionId, Long userId) {
        VoteEvent event = new VoteEvent(pollId, optionId, userId);
        rabbitTemplate.convertAndSend(
                pollExchange.getName(),
                "poll." + pollId,
                event
        );
        System.out.println("📤 Published vote event for poll " + pollId + " → option " + optionId);
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
