package com.example.rentacar.controller;

import com.example.rentacar.dto.request.LoginRequest;
import com.example.rentacar.dto.request.RegisterRequest;
import com.example.rentacar.dto.response.LoginResponse;
import com.example.rentacar.dto.response.ResponseMessage;
import com.example.rentacar.dto.response.VRResponse;
import com.example.rentacar.security.jwt.JwtUtils;
import com.example.rentacar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class UserJwtController { //login ve register işlemleri için

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * Registers a new user.
     * This endpoint is publicly accessible.
     *
     * @param registerRequest   The request body containing information for registering a new user.
     * @return                  ResponseEntity containing a VRResponse with information about the user registration process.
     *                          The response includes a message and success status.
     *                          The HTTP status in the response is HttpStatus.CREATED.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     */
    @PostMapping("/register")
    public ResponseEntity<VRResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        userService.saveUser(registerRequest);
        VRResponse response = new VRResponse();
        response.setMessage(ResponseMessage.REGISTER_RESPONSE_MESSAGE);
        response.setSuccess(true);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    /**
     * Authenticates a user by generating a JWT token upon successful login.
     * This endpoint is publicly accessible.
     *
     * @param loginRequest       The request body containing user credentials for authentication.
     * @return                   ResponseEntity containing a LoginResponse with the generated JWT token.
     *                           The HTTP status in the response is HttpStatus.OK.
     * @throws AuthenticationException Thrown if the user authentication fails.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        Authentication authentication = authenticationManager.
                authenticate(usernamePasswordAuthenticationToken);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateJwtToken(userDetails);

        LoginResponse loginResponse = new LoginResponse(jwtToken);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }


}
