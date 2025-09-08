# DAT250 Exercise Assignment 2 Report

## Technical Problems Encountered

1. **Lombok dependency not resolved**
    - Issue: Gradle failed to recognize Lombok classes (`@Data`, `@AllArgsConstructor`).
    - Resolution: Added the following to `build.gradle.kts`:
      ```kotlin
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
      ```

2. **Database configuration issues**
    - Issue: Spring Boot failed to start with H2, showing errors like `Failed to determine a suitable driver class`.
    - Resolution: Added H2 dependency and configured datasource:
      ```kotlin
      implementation("com.h2database:h2")
      ```

3. **H2 syntax errors on reserved words**
    - Issue: Using table name `user` caused SQL errors (`drop table if exists [*]user`).
    - Resolution: Renamed the table or added proper escaping in the entity:
      ```java
      @Table(name = "\"user\"")
      public class User {}
      ```

4. **Deprecated methods and APIs**
    - Issue: `MockBean`, `getStatusCodeValue()`, and `AutoCloseable` warnings.
    - Resolution: Rewrote tests using modern Mockito + JUnit 5 approaches and used `response.getStatusCode()` instead.

---

## Pending Issues / Limitations

1. **Endpoint Testing**
    - Only unit tests were written for controllers and services.
    - No full integration tests using `MockMvc` or live server tests.

2. **Validation**
    - Request validation annotations (`@Valid`, `@NotNull`) are not added to entities or controller methods.

3. **Datasource / Persistence**
    - The app uses H2 in-memory database for testing.
    - No persistent database setup (e.g., PostgresSQL) is configured for production.

4. **Swagger / OpenAPI**
    - OpenAPI documentation is not fully configured due to dependency issues.

---

## Key Code Additions

### User Controller Unit Test Snippet
```java
@Test
void testGetUserFound() {
    User user = new User();
    user.setId(1L);
    user.setUsername("Alice");
    when(userService.getUser(1L)).thenReturn(Optional.of(user));

    ResponseEntity<User> response = userController.getUser(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Alice", response.getBody().getUsername());
}
```

### Poll Controller Unit Test Snippet
```java
@Test
void testGetPollResults() {
    PollResult result = new PollResult("Option1", 3L);
    when(pollService.getPollResults(1L)).thenReturn(List.of(result));

    ResponseEntity<List<PollResult>> response = pollController.getPollResults(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
    assertEquals("Option1", response.getBody().get(0).getOptionCaption());
}
```

### GitHub Actions CI
Configured a workflow gradle.yml to automatically run unit tests on push

---

## Summary
The assignment focused on building Spring Boot services and controllers, writing unit tests for CRUD operations, and configuring CI with GitHub Actions. 
Core functionality works and unit tests pass, but endpoint validation, persistent database setup, and full integration tests are pending improvements.