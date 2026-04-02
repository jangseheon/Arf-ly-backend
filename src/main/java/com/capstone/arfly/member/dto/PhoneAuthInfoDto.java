package com.capstone.arfly.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PhoneAuthInfoDto {
    private String uid;
    private String phoneNumber;
}
