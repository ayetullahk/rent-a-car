package com.example.rentacar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactMessageRequest {

    @Size(min = 1, max = 50, message = "Your name '${validatedValue}must be between {min} and {max} chars long'")
    @NotBlank(message = "Please provide your name")
    private String name;

    @Size(min = 5, max = 50, message = "Your subject '${validatedValue}must be between {min} and {max} chars long'")
    @NotBlank(message = "Please provide message subject")
    private String subject;

    @Size(min = 20, max = 200, message = "Your body '${validatedValue}must be between {min} and {max} chars long'")
    @NotBlank(message = "Please provide message body")
    private String body;

    @Email(message = "Provide valid email")
    private String email;
}
