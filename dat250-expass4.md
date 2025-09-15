# DAT250 Exercise Assignment 4 Report

## Technical Problems Encountered

During the installation and use of Java Persistence Architecture (JPA) in our Spring Boot backend:

- **Database setup issues:** Initially, the H2 database connection was not configured correctly, leading to errors like `Failed to determine a suitable driver class`. This was resolved by including the H2 dependency and configuring a datasource in `application.properties`.
- **Entity mapping issues:** Some syntax errors occurred with H2 when table names conflicted with reserved keywords (e.g., `user`). This was resolved by using backticks or renaming tables/entities.
- **CORS and API testing:** While not directly JPA-related, enabling CORS was required to test CRUD operations from the frontend.

Overall, Spring Data JPA and repository classes made implementing CRUD operations straightforward. The `PollService` abstracts the JPA repository operations.

---

## Link to Code Repository

There is no specific code for experiment 2, as we did not follow the standard assignment test cases. All functionality was implemented using the `PollService` and JPA repositories.

---

## Inspecting Database Tables

- The `application.properties` file is configured to automatically create tables, columns, and relationships based on our JPA entity classes.
- Spring Data JPA handled table creation, column types, and relationships (e.g., foreign keys) automatically.
- The main entities resulting in database tables are: `User`, `Poll`, `VoteOption`, and `Vote`.

---

## Pending Issues / Future Work

- **Standard JPA test cases:** The provided assignment test cases were not used because our implementation relied on `PollService` for CRUD operations.
- **Database persistence:** Currently, H2 is used in-memory for testing. Persistence to an external database (PostgreSQL, MySQL) was not implemented.
- **Advanced queries:** No custom JPA queries (e.g., JPQL, criteria API) were implemented; only basic CRUD operations were tested.
- **Integration with frontend:** Full integration with the frontend will require a live database setup for persistence across application restarts.
