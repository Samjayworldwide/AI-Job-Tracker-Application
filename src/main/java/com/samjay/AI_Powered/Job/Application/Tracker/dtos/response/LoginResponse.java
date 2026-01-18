package com.samjay.AI_Powered.Job.Application.Tracker.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String firstname;

    private String lastname;

    private String token;

}