package no.hvl.dat250.services;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PollRedisService {

    private static final String POLL_PREFIX = "poll:";

    private final RedisTemplate<String, Object> redisTemplate;

    public PollRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void savePoll(String pollId, String title, Map<String, Long> options) {
        HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
        hashOps.put(POLL_PREFIX + pollId, "title", title);
        options.forEach((option, votes) ->
                hashOps.put(POLL_PREFIX + pollId, option, votes)
        );
    }

    public Map<Object, Object> getPoll(String pollId) {
        return redisTemplate.opsForHash().entries(POLL_PREFIX + pollId);
    }

    public void incrementVote(String pollId, String option) {
        redisTemplate.opsForHash().increment(POLL_PREFIX + pollId, option, 1);
    }
}
