package com.jobportal.dto.candidate;

import com.jobportal.enums.EmploymentType;
import com.jobportal.enums.RemotePreference;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenceRequest {

    @Size(max = 100, message = "Preferred job title cannot exceed 100 characters")
    private String preferredJobTitle;

    @Size(max = 150, message = "Preferred location cannot exceed 150 characters")
    private String preferredLocation;

    @DecimalMin(value = "0.0", message = "Minimum salary must not be negative")
    private BigDecimal minimumSalary;

    @DecimalMin(value = "0.0", message = "Maximum salary must not be negative")
    private BigDecimal maximumSalary;

    private EmploymentType preferredEmploymentType;

    private RemotePreference remotePreference;
}
