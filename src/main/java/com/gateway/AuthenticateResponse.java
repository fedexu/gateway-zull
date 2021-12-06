package com.gateway;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticateResponse {

    private String jwttoken;
    private String roles;

}
