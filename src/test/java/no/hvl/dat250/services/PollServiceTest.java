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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollServiceTest {

    private UserRepository userRepo;
    private PollRepository pollRepo;
    private VoteOptionRepository optionRepo;
    private VoteRepository voteRepo;
    private PollService pollService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        pollRepo = mock(PollRepository.class);
        optionRepo = mock(VoteOptionRepository.class);
        voteRepo = mock(VoteRepository.class);
        PollCacheService pollCacheService = mock(PollCacheService.class);
        pollService = new PollService(userRepo, pollRepo, optionRepo, voteRepo, pollCacheService);
    }

    @Test
    void testCreatePoll() {
        User user = new User();
        user.setId(1L);
        Poll poll = new Poll();
        poll.setQuestion("Favorite color?");
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(pollRepo.save(any(Poll.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Optional<Poll> created = pollService.createPoll(1L, poll);
        assertTrue(created.isPresent());
        assertEquals(user, created.get().getCreatedBy());
        assertEquals("Favorite color?", created.get().getQuestion());
    }

    @Test
    void testAddOption() {
        Poll poll = new Poll();
        poll.setId(1L);
        VoteOption option = new VoteOption();
        option.setCaption("Red");
        when(pollRepo.findById(1L)).thenReturn(Optional.of(poll));
        when(optionRepo.save(any(VoteOption.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Optional<VoteOption> savedOption = pollService.addOption(1L, option);
        assertTrue(savedOption.isPresent());
        assertEquals(poll, savedOption.get().getPoll());
    }

    @Test
    void testCastVote() {
        User user = new User();
        user.setId(1L);
        Poll poll = new Poll();
        poll.setId(1L);
        poll.setValidUntil(Instant.now().plusSeconds(3600));
        VoteOption option = new VoteOption();
        option.setId(1L);
        option.setPoll(poll);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(optionRepo.findById(1L)).thenReturn(Optional.of(option));
        when(voteRepo.save(any(Vote.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Optional<Vote> vote = pollService.castVote(1L, 1L);
        assertTrue(vote.isPresent());
        assertEquals(user, vote.get().getVoter());
        assertEquals(option, vote.get().getOption());
    }

    @Test
    void testGetPollResults() {
        Poll poll = new Poll();
        poll.setId(1L);
        VoteOption option1 = new VoteOption();
        option1.setCaption("Red");
        option1.setVotes(List.of(new Vote(), new Vote())); // 2 votes
        VoteOption option2 = new VoteOption();
        option2.setCaption("Blue");
        option2.setVotes(List.of(new Vote())); // 1 vote
        poll.setOptions(List.of(option1, option2));
        when(pollRepo.findById(1L)).thenReturn(Optional.of(poll));
        List<PollResult> results = pollService.getPollResults(1L);
        assertEquals(2, results.size());
        assertEquals("Red", results.getFirst().getOptionCaption());
        assertEquals(2, results.getFirst().getVoteCount());
    }
}