package com.jobportal.dto.candidate;

import com.jobportal.enums.EmploymentType;
import com.jobportal.enums.RemotePreference;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceResponse {
    private Long id;
    private Long candidateId;
    private String preferredJobTitle;
    private String preferredLocation;
    private BigDecimal minimumSalary;
    private BigDecimal maximumSalary;
    private EmploymentType preferredEmploymentType;
    private RemotePreference remotePreference;
}
