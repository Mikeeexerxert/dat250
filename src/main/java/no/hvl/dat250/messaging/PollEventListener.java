package no.hvl.dat250.messaging;

import no.hvl.dat250.dto.VoteEvent;
import no.hvl.dat250.entities.Vote;
import no.hvl.dat250.entities.VoteOption;
import no.hvl.dat250.repositories.VoteOptionRepository;
import no.hvl.dat250.repositories.VoteRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PollEventListener {

    private final VoteRepository voteRepo;
    private final VoteOptionRepository optionRepo;

    public PollEventListener(VoteRepository voteRepo, VoteOptionRepository optionRepo) {
        this.voteRepo = voteRepo;
        this.optionRepo = optionRepo;
    }

    @RabbitListener(queues = "pollApp.queue")
    public void handleVoteEvent(VoteEvent event) {
        if (event.getOptionId() == null) return;

        VoteOption option = optionRepo.findById(event.getOptionId()).orElse(null);
        if (option == null) return;

        Vote vote = new Vote();
        vote.setOption(option);
        vote.setPublishedAt(Instant.now());

        voteRepo.save(vote);

        System.out.println("Vote received via MQ: poll " + event.getPollId() + ", option " + event.getOptionId());
    }
}