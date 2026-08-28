package com.jobportal.service;

import com.jobportal.dto.candidate.EducationRequest;
import com.jobportal.dto.candidate.EducationResponse;
import com.jobportal.entity.Candidate;
import com.jobportal.entity.Education;
import com.jobportal.entity.User;
import com.jobportal.exception.BadRequestException;
import com.jobportal.exception.ResourceNotFoundException;
import com.jobportal.repository.EducationRepository;
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
public class EducationService {

    private final CandidateService candidateService;
    private final EducationRepository educationRepository;

    @Transactional(readOnly = true)
    public List<EducationResponse> getEducationList(User user) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);
        return educationRepository.findByCandidateId(candidate.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EducationResponse createEducation(User user, EducationRequest request) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);

        validateDates(request);

        Education education = Education.builder()
                .candidate(candidate)
                .institution(request.getInstitution().trim())
                .degree(request.getDegree().trim())
                .fieldOfStudy(request.getFieldOfStudy().trim())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .build();

        Education saved = educationRepository.save(education);
        log.info("Created education record ID: {} for Candidate ID: {}", saved.getId(), candidate.getId());

        return toResponse(saved);
    }

    @Transactional
    public EducationResponse updateEducation(User user, Long educationId, EducationRequest request) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);

        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", educationId));

        // Ownership verification (IDOR / BOLA Prevention)
        if (!education.getCandidate().getId().equals(candidate.getId())) {
            throw new AccessDeniedException("You do not own this education resource");
        }

        validateDates(request);

        education.setInstitution(request.getInstitution().trim());
        education.setDegree(request.getDegree().trim());
        education.setFieldOfStudy(request.getFieldOfStudy().trim());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setDescription(request.getDescription());

        Education saved = educationRepository.save(education);
        log.info("Updated education record ID: {} for Candidate ID: {}", saved.getId(), candidate.getId());

        return toResponse(saved);
    }

    @Transactional
    public void deleteEducation(User user, Long educationId) {
        Candidate candidate = candidateService.getOrCreateCandidate(user);

        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", educationId));

        // Ownership verification (IDOR / BOLA Prevention)
        if (!education.getCandidate().getId().equals(candidate.getId())) {
            throw new AccessDeniedException("You do not own this education resource");
        }

        educationRepository.delete(education);
        log.info("Deleted education record ID: {} from Candidate ID: {}", educationId, candidate.getId());
    }

    private void validateDates(EducationRequest request) {
        if (request.getEndDate() != null && request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date");
        }
    }

    private EducationResponse toResponse(Education edu) {
        return EducationResponse.builder()
                .id(edu.getId())
                .candidateId(edu.getCandidate().getId())
                .institution(edu.getInstitution())
                .degree(edu.getDegree())
                .fieldOfStudy(edu.getFieldOfStudy())
                .startDate(edu.getStartDate())
                .endDate(edu.getEndDate())
                .description(edu.getDescription())
                .build();
    }
}
