package no.hvl.dat250.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@RedisHash("Vote")
@Table(name = "vote")
public class Vote implements Serializable {

    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant publishedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private transient User voter;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private transient VoteOption option;
}