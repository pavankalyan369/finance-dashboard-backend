package com.backend.dto;

import com.backend.entity.Gender;
import com.backend.entity.Occupation;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String address;
    private Integer age;
    private Gender gender;
    private Occupation occupation;
}
