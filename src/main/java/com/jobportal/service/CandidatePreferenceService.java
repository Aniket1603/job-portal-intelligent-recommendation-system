package com.jobportal.service;

import com.jobportal.dto.candidate.PreferenceRequest;
import com.jobportal.dto.candidate.PreferenceResponse;
import com.jobportal.entity.Candidate;
import com.jobportal.entity.CandidatePreference;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.repository.CandidatePreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidatePreferenceService {

    private final CandidateService candidateService;
    private final CandidatePreferenceRepository candidatePreferenceRepository;

    @Transactional
    public CandidatePreference getOrCreatePreferences(Candidate candidate) {
        return candidatePreferenceRepository.findByCandidateId(candidate.getId())
                .orElseGet(() -> {
                    log.info("Auto-creating CandidatePreference record for Candidate ID: {}", candidate.getId());
                    CandidatePreference preference = CandidatePreference.builder()
                            .candidate(candidate)
                            .build();
                    return candidatePreferenceRepository.save(preference);
                });
    }

    @Transactional(readOnly = true)
    public PreferenceResponse getPreferences(User user) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);
        CandidatePreference preference = getOrCreatePreferences(candidate);
        return toResponse(preference);
    }

    @Transactional
    public PreferenceResponse updatePreferences(User user, PreferenceRequest request) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);
        CandidatePreference preference = getOrCreatePreferences(candidate);

        // Validation for salary range
        if (request.getMinimumSalary() != null && request.getMaximumSalary() != null) {
            if (request.getMinimumSalary().compareTo(request.getMaximumSalary()) > 0) {
                throw new BadRequestException("Minimum salary must be less than or equal to maximum salary");
            }
        }

        preference.setPreferredJobTitle(request.getPreferredJobTitle());
        preference.setPreferredLocation(request.getPreferredLocation());
        preference.setMinimumSalary(request.getMinimumSalary());
        preference.setMaximumSalary(request.getMaximumSalary());
        preference.setPreferredEmploymentType(request.getPreferredEmploymentType());
        preference.setRemotePreference(request.getRemotePreference());

        CandidatePreference saved = candidatePreferenceRepository.save(preference);
        log.info("Updated preferences for Candidate ID: {}", candidate.getId());

        return toResponse(saved);
    }

    private PreferenceResponse toResponse(CandidatePreference pref) {
        return PreferenceResponse.builder()
                .id(pref.getId())
                .candidateId(pref.getCandidate().getId())
                .preferredJobTitle(pref.getPreferredJobTitle())
                .preferredLocation(pref.getPreferredLocation())
                .minimumSalary(pref.getMinimumSalary())
                .maximumSalary(pref.getMaximumSalary())
                .preferredEmploymentType(pref.getPreferredEmploymentType())
                .remotePreference(pref.getRemotePreference())
                .build();
    }
}
