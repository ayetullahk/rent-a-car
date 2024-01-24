package com.example.rentacar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserUpdateRequest {
    @NotBlank(message = "Please provide your First name")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Please provide your Last name")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Please provide your email")
    @Size(max = 80, min = 5)
    private String email;

    @Size(min = 4, max = 20, message = "Please provide Correct Size of Password")
    @NotBlank(message = "Please provide your Password")
    private String password;

    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\(?\\d{1,4}\\)?[- ]?\\d{1,9}$",
            message = "Please provide valid phone number")
    @NotBlank(message = "Please provide your phone number")
    @Size(max = 14)
    private String phoneNumber;

    @NotBlank(message = "Please provide your address")
    @Size(max = 100)
    private String address;

    @NotBlank(message = "Please provide your zip-code")
    @Size(max = 15)
    private String zipCode;

    private Boolean builtin;

    private Set<String> roles;

}
