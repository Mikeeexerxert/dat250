package no.hvl.dat250.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PollResult {
    private String optionCaption;
    private Long voteCount;
}