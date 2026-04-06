package com.backend.dto.analytics;

import com.backend.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserProfileResponseDTO {

    private UUID id;        //
    private String fullName;
    private Integer age;
    private Gender gender;
    private String location;
}