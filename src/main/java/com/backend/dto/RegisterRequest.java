package com.backend.dto;

import com.backend.entity.Gender;
import com.backend.entity.Occupation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String email;
    private String password;

    // PROFILE DATA
    private String fullName;
    private String phone;
    private String address;
    private Integer age;
    private Gender gender;
    private Occupation occupation;
}