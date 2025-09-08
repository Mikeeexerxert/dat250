package no.hvl.dat250.controllers;

import no.hvl.dat250.dto.PollResult;
import no.hvl.dat250.entities.Poll;
import no.hvl.dat250.services.PollService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PollControllerTest {

    private PollController pollController;

    @Mock
    private PollService pollService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        pollController = new PollController(pollService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void testGetAllPolls() {
        Poll poll = new Poll();
        poll.setId(1L);
        poll.setQuestion("Sample?");
        when(pollService.getAllPolls()).thenReturn(List.of(poll));
        List<Poll> result = pollController.getAllPolls();
        assertEquals(1, result.size());
        assertEquals("Sample?", result.getFirst().getQuestion());
    }

    @Test
    void testGetPollFound() {
        Poll poll = new Poll();
        poll.setId(1L);
        poll.setQuestion("Found?");
        when(pollService.getPoll(1L)).thenReturn(Optional.of(poll));
        ResponseEntity<Poll> response = pollController.getPoll(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Found?", response.getBody().getQuestion());
    }

    @Test
    void testGetPollNotFound() {
        when(pollService.getPoll(99L)).thenReturn(Optional.empty());
        ResponseEntity<Poll> response = pollController.getPoll(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testCreatePoll() {
        Poll poll = new Poll();
        poll.setQuestion("New poll");
        when(pollService.createPoll(1L, poll)).thenReturn(Optional.of(poll));
        ResponseEntity<Poll> response = pollController.createPoll(1L, poll);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New poll", response.getBody().getQuestion());
    }

    @Test
    void testDeletePollSuccess() {
        when(pollService.deletePoll(1L)).thenReturn(true);
        ResponseEntity<Void> response = pollController.deletePoll(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testDeletePollFail() {
        when(pollService.deletePoll(1L)).thenReturn(false);
        ResponseEntity<Void> response = pollController.deletePoll(1L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetPollResults() {
        PollResult result = new PollResult("Option1", 3L);
        when(pollService.getPollResults(1L)).thenReturn(List.of(result));
        ResponseEntity<List<PollResult>> response = pollController.getPollResults(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Option1", response.getBody().getFirst().getOptionCaption());
        assertEquals(3L, response.getBody().getFirst().getVoteCount());
    }
}