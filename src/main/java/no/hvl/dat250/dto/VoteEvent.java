package no.hvl.dat250.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VoteEvent {
    private Long pollId;
    private Long optionId;
    private Long userId; // optional (null for anonymous votes)
}
