package com.backend.service;

import com.backend.dto.ProfileUpdateRequest;
import com.backend.entity.*;
import com.backend.exception.BadRequestException;
import com.backend.exception.ResourceNotFoundException;
import com.backend.repository.ActivationTokenRepository;
import com.backend.repository.ProfileRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ActivationTokenRepository tokenRepository;
    private final ProfileRepository profileRepository;

    public User getByEmail(String email) {
        return userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public void deactivate(String email) {

        User user = getByEmail(email);

        if (user.getStatus() == AccountStatus.INACTIVE) {
            throw new BadRequestException("Already inactive");
        }

        user.setStatus(AccountStatus.INACTIVE);
        user.setDeactivatedBy(DeactivationReason.USER);

        // invalidate JWT
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
    }

    public void delete(String email) {

        User user = getByEmail(email);

        // already in soft delete
        if (user.getDeletedAt() != null && user.getStatus() == AccountStatus.INACTIVE) {
            throw new BadRequestException("Account already deactivated");
        }

        //  SOFT DELETE (GRACE PERIOD STARTS)
        user.setStatus(AccountStatus.INACTIVE);
        user.setDeactivatedBy(DeactivationReason.USER);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeleted(false); //  MUST BE FALSE

        // invalidate JWT
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
    }

    public Profile getProfile(String email) {

        User user = getByEmail(email);

        return profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    public Profile updateProfile(String email, ProfileUpdateRequest request) {

        User user = getByEmail(email);

        Profile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());
        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setOccupation(request.getOccupation());

        return profileRepository.save(profile);
    }


}