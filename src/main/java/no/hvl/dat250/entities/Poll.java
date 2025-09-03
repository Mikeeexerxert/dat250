package no.hvl.dat250.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// Poll
@Entity
@Getter
@Setter
@ToString
public class Poll {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;
    private Instant publishedAt;
    private Instant validUntil;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User createdBy;

    // A poll has many options
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<VoteOption> options = new ArrayList<>();
}