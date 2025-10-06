package no.hvl.dat250.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserSessionService {

    private static final String LOGGED_IN_USERS = "logged_in_users";

    private final RedisTemplate<String, Object> redisTemplate;

    public UserSessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void login(String username) {
        redisTemplate.opsForSet().add(LOGGED_IN_USERS, username);
    }

    public void logout(String username) {
        redisTemplate.opsForSet().remove(LOGGED_IN_USERS, username);
    }

    public Set<Object> getLoggedInUsers() {
        Set<Object> users = redisTemplate.opsForSet().members(LOGGED_IN_USERS);
        return users != null ? users : Set.of();
    }

    public boolean isLoggedIn(String username) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(LOGGED_IN_USERS, username));
    }
}
