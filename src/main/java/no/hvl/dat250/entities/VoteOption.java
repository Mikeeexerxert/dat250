package no.hvl.dat250.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

// VoteOption
@Entity
@Getter
@Setter
@ToString
public class VoteOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String caption;
    private int presentationOrder;

    @ManyToOne
    @JoinColumn(name = "poll_id")
    @JsonBackReference
    private Poll poll;

    // Votes linked to this option
    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes = new ArrayList<>();
}