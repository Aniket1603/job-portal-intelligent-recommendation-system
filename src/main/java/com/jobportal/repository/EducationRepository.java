package com.jobportal.repository;

import com.jobportal.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByCandidateId(Long candidateId);
    Optional<Education> findByIdAndCandidateId(Long id, Long candidateId);
}
