package com.orrs.repository;

import com.orrs.model.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    @Query("select p from PatientProfile p where p.user.id = :userId")
    PatientProfile findByUserId(@Param("userId") Long userId);
}
