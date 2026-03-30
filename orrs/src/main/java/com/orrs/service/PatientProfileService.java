package com.orrs.service;

import com.orrs.dto.PatientProfileDTO;
import com.orrs.model.PatientProfile;
import com.orrs.model.User;
import com.orrs.repository.PatientProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;

    public PatientProfile getOrCreate(User user) {
        PatientProfile p = patientProfileRepository.findByUserId(user.getId());
        if (p != null) return p;
        PatientProfile np = new PatientProfile();
        np.setUser(user);
        // keep fields empty; completion is handled by UI/validation
        return patientProfileRepository.save(np);
    }

    public List<String> missingFields(User user) {
        PatientProfile p = getOrCreate(user);
        return p.missingRequiredFields();
    }

    public boolean isComplete(User user) {
        return getOrCreate(user).isProfileComplete();
    }

    @Transactional
    public PatientProfile update(User user, PatientProfileDTO dto) {
        PatientProfile p = getOrCreate(user);

        p.setAge(dto.getAge());
        p.setGender(dto.getGender());
        p.setHeightCm(dto.getHeightCm());
        p.setDominantHand(dto.getDominantHand());

        p.setCancerType(dto.getCancerType());
        p.setCancerStage(dto.getCancerStage());
        p.setPrimarySite(dto.getPrimarySite());
        p.setTreatmentType(dto.getTreatmentType());
        p.setChemoAgents(dto.getChemoAgents());
        p.setRadiationSite(dto.getRadiationSite());
        p.setSurgeryPerformed(dto.getSurgeryPerformed());

        p.setTreatmentStartDate(dto.getTreatmentStartDate());
        p.setTreatmentEndDate(dto.getTreatmentEndDate());

        p.setWeightBeforeTreatment(dto.getWeightBeforeTreatment());
        p.setWeightAfterTreatment(dto.getWeightAfterTreatment());

        p.setComorbidities(dto.getComorbidities());
        p.setCurrentPainAreas(dto.getCurrentPainAreas());
        p.setActivityLevel(dto.getActivityLevel());
        p.setBaselineNeuropathy(dto.getBaselineNeuropathy());

        return patientProfileRepository.save(p);
    }
}
