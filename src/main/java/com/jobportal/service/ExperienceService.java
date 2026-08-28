package com.jobportal.service;

import com.jobportal.dto.candidate.ExperienceRequest;
import com.jobportal.dto.candidate.ExperienceResponse;
import com.jobportal.entity.Candidate;
import com.jobportal.entity.Experience;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.ExperienceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final CandidateService candidateService;
    private final ExperienceRepository experienceRepository;

    @Transactional(readOnly = true)
    public List<ExperienceResponse> getExperienceList(User user) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);
        return experienceRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExperienceResponse createExperience(User user, ExperienceRequest request) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);

        validateExperienceRequest(request);

        Experience experience = Experience.builder()
                .candidate(candidate)
                .companyName(request.getCompanyName().trim())
                .jobTitle(request.getJobTitle().trim())
                .employmentType(request.getEmploymentType())
                .location(request.getLocation() != null ? request.getLocation().trim() : null)
                .startDate(request.getStartDate())
                .endDate(request.isCurrentlyWorking() ? null : request.getEndDate())
                .currentlyWorking(request.isCurrentlyWorking())
                .description(request.getDescription())
                .build();

        Experience saved = experienceRepository.save(experience);
        log.info("Created experience record ID: {} for Candidate ID: {}", saved.getId(), candidate.getId());

        return toResponse(saved);
    }

    @Transactional
    public ExperienceResponse updateExperience(User user, Long experienceId, ExperienceRequest request) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);

        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", experienceId));

        // Ownership verification (IDOR / BOLA Prevention)
        if (!experience.getCandidate().getId().equals(candidate.getId())) {
            throw new AccessDeniedException("You do not own this experience resource");
        }

        validateExperienceRequest(request);

        experience.setCompanyName(request.getCompanyName().trim());
        experience.setJobTitle(request.getJobTitle().trim());
        experience.setEmploymentType(request.getEmploymentType());
        experience.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.isCurrentlyWorking() ? null : request.getEndDate());
        experience.setCurrentlyWorking(request.isCurrentlyWorking());
        experience.setDescription(request.getDescription());

        Experience saved = experienceRepository.save(experience);
        log.info("Updated experience record ID: {} for Candidate ID: {}", saved.getId(), candidate.getId());

        return toResponse(saved);
    }

    @Transactional
    public void deleteExperience(User user, Long experienceId) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);

        Experience experience = experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ResourceNotFoundException("Experience", "id", experienceId));

        // Ownership verification (IDOR / BOLA Prevention)
        if (!experience.getCandidate().getId().equals(candidate.getId())) {
            throw new AccessDeniedException("You do not own this experience resource");
        }

        experienceRepository.delete(experience);
        log.info("Deleted experience record ID: {} from Candidate ID: {}", experienceId, candidate.getId());
    }

    private void validateExperienceRequest(ExperienceRequest request) {
        if (request.isCurrentlyWorking()) {
            if (request.getEndDate() != null) {
                throw new BadRequestException("End date must be null if currently working");
            }
        } else {
            if (request.getEndDate() == null) {
                throw new BadRequestException("End date is required if not currently working");
            }
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new BadRequestException("Start date must be before end date");
            }
        }
    }

    private ExperienceResponse toResponse(Experience exp) {
        return ExperienceResponse.builder()
                .id(exp.getId())
                .candidateId(exp.getCandidate().getId())
                .companyName(exp.getCompanyName())
                .jobTitle(exp.getJobTitle())
                .employmentType(exp.getEmploymentType())
                .location(exp.getLocation())
                .startDate(exp.getStartDate())
                .endDate(exp.getEndDate())
                .currentlyWorking(exp.isCurrentlyWorking())
                .description(exp.getDescription())
                .build();
    }
}
