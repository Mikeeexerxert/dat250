package no.hvl.dat250.services;

import no.hvl.dat250.dto.PollResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PollCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String POLL_PREFIX = "poll:";

    public PollCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String pollKey(Long pollId) {
        return POLL_PREFIX + pollId;
    }

    public boolean isCached(Long pollId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(pollKey(pollId)));
    }

    public List<PollResult> getCachedResults(Long pollId) {
        Object cached = redisTemplate.opsForValue().get(pollKey(pollId));
        if (cached instanceof List<?> list) {
            // Safely cast and collect to immutable list
            return list.stream()
                    .map(PollResult.class::cast)
                    .toList();
        }
        return List.of();
    }


    public void cacheResults(Long pollId, List<PollResult> results) {
        redisTemplate.opsForValue().set(pollKey(pollId), results);
    }

    public void invalidate(Long pollId) {
        redisTemplate.delete(pollKey(pollId));
    }
}