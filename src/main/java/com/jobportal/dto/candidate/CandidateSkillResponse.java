package com.jobportal.dto.candidate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSkillResponse {
    private Long id;
    private Long skillId;
    private String name;
    private Double yearsOfExperience;
}
