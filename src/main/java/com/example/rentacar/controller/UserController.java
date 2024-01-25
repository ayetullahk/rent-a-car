package com.example.rentacar.controller;

import com.example.rentacar.dto.ContactMessageDTO;
import com.example.rentacar.dto.UserDTO;
import com.example.rentacar.dto.request.AdminUserUpdateRequest;
import com.example.rentacar.dto.request.UpdatePasswordRequest;
import com.example.rentacar.dto.request.UserUpdateRequest;
import com.example.rentacar.dto.response.ResponseMessage;
import com.example.rentacar.dto.response.VRResponse;
import com.example.rentacar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * Retrieves a list of all users.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @return                   ResponseEntity containing a list of UserDTOs with information about all users.
     *                           The HTTP status in the response is HttpStatus.OK.
     * @throws UnauthorizedException Thrown if the user is not authorized to access the list of users.
     */
    @GetMapping("/auth/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> allUsers = userService.getAllUsers();

        return ResponseEntity.ok(allUsers);
    }

    /**
     * Retrieves the details of the authenticated user.
     * This endpoint is accessible to users with either the ADMIN or CUSTOMER role.
     *
     * @return                   ResponseEntity containing a UserDTO with information about the authenticated user.
     *                           The HTTP status in the response is HttpStatus.OK.
     * @throws UnauthorizedException Thrown if the user is not authorized to access their own details.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')or hasRole('CUSTOMER')")
    public ResponseEntity<UserDTO> getUser() {
        UserDTO userDTO = userService.getPrincipal();

        return ResponseEntity.ok(userDTO);
    }

    /**
     * Retrieves a paginated list of all users.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param page                The page number (zero-based) to be retrieved.
     * @param size                The number of items per page.
     * @param prop                The property by which the result should be sorted.
     * @param direction           The sorting direction, either ASC or DESC.
     * @return                    ResponseEntity containing a Page of UserDTOs with information about users.
     *                            The response includes pagination details.
     *                            The HTTP status in the response is HttpStatus.OK.
     * @throws UnauthorizedException Thrown if the user is not authorized to access the paginated list of users.
     */
    @GetMapping("/auth/pages")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsersByPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sort") String prop, // neye göre sıralanacağı
            @RequestParam(value = "direction", required = false, defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, prop));

        Page<UserDTO> userDTOPage = userService.getUserPage(pageable);

        return ResponseEntity.ok(userDTOPage);

    }

    /**
     * Retrieves the details of a user by their identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                  The identifier of the user to be retrieved.
     * @return                    ResponseEntity containing a UserDTO with information about the specified user.
     *                            The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException  Thrown if the specified user is not found.
     * @throws UnauthorizedException Thrown if the user is not authorized to access the details of the specified user.
     */
    @GetMapping("/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);

        return ResponseEntity.ok(userDTO);
    }

    /**
     * Updates the password of the authenticated user.
     * This endpoint is accessible to users with either the ADMIN or CUSTOMER role.
     *
     * @param updatePasswordRequest The request body containing information for updating the password.
     * @return                      ResponseEntity containing a VRResponse with information about the password update process.
     *                              The response includes a message and success status.
     *                              The HTTP status in the response is HttpStatus.OK.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     * @throws UnauthorizedException Thrown if the user is not authorized to update their own password.
     */
    @PatchMapping("/auth")
    @PreAuthorize("hasRole('ADMIN')or hasRole('CUSTOMER')")
    public ResponseEntity<VRResponse> updatePassword(@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        userService.updatePassword(updatePasswordRequest);

        VRResponse response = new VRResponse();
        response.setMessage(ResponseMessage.PASSWORD_CHANGED_RESPONSE_MESSAGE);
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the details of the authenticated user.
     * This endpoint is accessible to users with either the ADMIN or CUSTOMER role.
     *
     * @param userUpdateRequest     The request body containing information for updating the user details.
     * @return                      ResponseEntity containing a VRResponse with information about the user update process.
     *                              The response includes a message and success status.
     *                              The HTTP status in the response is HttpStatus.OK.
     * @throws InvalidInputException Thrown if the input parameters are invalid.
     * @throws UnauthorizedException Thrown if the user is not authorized to update their own details.
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')or hasRole('CUSTOMER')")
    public ResponseEntity<VRResponse> updateUser(@Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        userService.updateUser(userUpdateRequest);

        VRResponse response = new VRResponse();
        response.setMessage(ResponseMessage.USER_UPDATE_RESPONSE_MESSAGE);
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    /**
     * Updates the authentication details of a user by their identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                       The identifier of the user to be updated.
     * @param adminUserUpdateRequest   The request body containing information for updating the user authentication details.
     * @return                         ResponseEntity containing a VRResponse with information about the user update process.
     *                                 The response includes a message and success status.
     *                                 The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException       Thrown if the specified user is not found.
     * @throws InvalidInputException    Thrown if the input parameters are invalid.
     * @throws UnauthorizedException    Thrown if the user is not authorized to update the authentication details of the specified user.
     */
    @PutMapping("/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse> updateUserAuth(@PathVariable Long id, @Valid @RequestBody
    AdminUserUpdateRequest adminUserUpdateRequest) {

        userService.updateUserAuth(id, adminUserUpdateRequest);
        VRResponse response = new VRResponse();
        response.setMessage(ResponseMessage.USER_UPDATE_RESPONSE_MESSAGE);
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a user by their identifier.
     * This endpoint is restricted to users with the ADMIN role.
     *
     * @param id                   The identifier of the user to be deleted.
     * @return                     ResponseEntity containing a VRResponse with information about the user deletion process.
     *                             The response includes a message and success status.
     *                             The HTTP status in the response is HttpStatus.OK.
     * @throws NotFoundException   Thrown if the specified user is not found.
     * @throws UnauthorizedException Thrown if the user is not authorized to delete the specified user.
     */
    @DeleteMapping("/{id}/auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VRResponse>deleteUser(@PathVariable Long id){
        userService.removeUserById(id);

        VRResponse response = new VRResponse();
        response.setMessage(ResponseMessage.USER_DELETE_RESPONSE_MESSAGE);
        response.setSuccess(true);

        return ResponseEntity.ok(response);
    }

}
