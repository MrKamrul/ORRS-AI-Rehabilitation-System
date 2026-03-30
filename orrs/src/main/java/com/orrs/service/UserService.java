package com.orrs.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.orrs.model.User;
import com.orrs.model.PatientProfile;
import com.orrs.repository.PatientProfileRepository;
import com.orrs.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // Register user
    public User registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (user.getRole() == null) {
            user.setRole(User.Role.PATIENT); // default role
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Save user first
        User saved = userRepository.save(user);

        // Auto-create a basic PatientProfile for patient users so patient endpoints work immediately
        if (saved.getRole() == User.Role.PATIENT) {
            PatientProfile existing = patientProfileRepository.findByUserId(saved.getId());
            if (existing == null) {
                PatientProfile profile = new PatientProfile();
                profile.setUser(saved);
                PatientProfile created = patientProfileRepository.save(profile);
                // link for convenience (some controllers/services may use user.getPatientProfile())
                saved.setPatientProfile(created);
                userRepository.save(saved);
            }
        }

        return saved;
    }

    // Validate login
    public User validateLogin(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    // Optional: Get user by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
