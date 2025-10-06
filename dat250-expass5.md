# DAT250 Exercise Assignment 5 Report

## Technical Problems Encountered

### 1. NullPointerException warnings with RedisTemplate methods 
   Some Redis operations like `hasKey()` and `opsForSet().isMember()` were nullable and could throw `NullPointerException`.  
   **Resolution:** Wrapped the calls in null-safe checks. For example:

   ```java
   public boolean isLoggedIn(String username) {
       Boolean result = redisTemplate.opsForSet().isMember(LOGGED_IN_USERS, username);
       return result != null && result;
   }

   public boolean isCached(Long pollId) {
       Boolean cached = redisTemplate.hasKey(pollKey(pollId));
       return cached != null && cached;
   }
   ```
   
### 2. Conflicting Bean Definitions Between JPA and Redis Repositories

After adding Redis repositories, Spring failed to start due to duplicate bean names for repositories like `VoteRepository`.  
**Resolution:** Allow bean overriding. In application properties: spring.main.allow-bean-definition-overriding=true.
This will allow Spring to override the JPA bean with the Redis one. Only use this for quick testing.

### 3. Serialization Issues for Redis

Java entities like `Poll` and `VoteOption` needed to be serializable for caching.  
**Resolution:** Added `implements Serializable` to entity classes and marked non-serializable JPA relations as `transient` in Redis cache DTOs.

### 4. Mapping JPA Entities to Redis Cache Objects

Needed to transform `Poll` and `VoteOption` into simplified objects suitable for storing in Redis (denormalized view).  
**Resolution:** Created `PollCacheService` that caches poll results using Redis `ValueOperations` as a list of `PollResult` objects.

## Pending Issues

- **Unit testing Redis cache:** Currently, no automated tests for Redis cache service.
- **Live integration:** Some endpoints like voting and poll results are not fully hooked into Redis cache in the absence of a persistent database.
- **Error handling:** Handling of nulls and missing keys is basic; could be improved with optional TTL (time-to-live) for cached polls.