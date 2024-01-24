package com.example.rentacar.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @Size(max = 50)
    @NotBlank(message = "Please provide your First Name")
    private String firstName;
    @Size(max = 50)
    @NotBlank(message = "Please provide your Last Name")
    private String lastName;
    @Size(min = 5, max = 80)
    @Email(message = "Please provide valid e-mail")
    private String email;
    @Size(min = 4, max = 20, message = "Please provide Correct Size of Password")
    @NotBlank(message = "Please provide your Password")
    private String password;
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\(?\\d{1,4}\\)?[- ]?\\d{1,9}$",
            message = "Please provide valid phone number")
    @NotBlank(message = "Please provide your phone number")
    @Size(max = 14, min = 14)
    private String phoneNumber;
    @Size(max = 100)
    @NotBlank(message = "Please provide your address")
    private String address;
    @Size(max = 15)
    @NotBlank(message = "Please provide your zip code")
    private String zipCode;


}
