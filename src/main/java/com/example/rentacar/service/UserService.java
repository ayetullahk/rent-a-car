package com.example.rentacar.service;

import com.example.rentacar.domain.Role;
import com.example.rentacar.domain.User;
import com.example.rentacar.domain.enums.RoleType;
import com.example.rentacar.dto.UserDTO;
import com.example.rentacar.dto.request.AdminUserUpdateRequest;
import com.example.rentacar.dto.request.RegisterRequest;
import com.example.rentacar.dto.request.UpdatePasswordRequest;
import com.example.rentacar.dto.request.UserUpdateRequest;
import com.example.rentacar.exception.BadRequestException;
import com.example.rentacar.exception.ConflictException;
import com.example.rentacar.exception.ResourceNotFoundException;
import com.example.rentacar.exception.message.ErrorMessage;
import com.example.rentacar.mapper.UserMapper;
import com.example.rentacar.repository.UserRepository;
import com.example.rentacar.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
public class UserService {

    private UserRepository userRepository;

    private RoleService roleService;

    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;

    private ReservationService reservationService;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService, @Lazy PasswordEncoder passwordEncoder,
                       UserMapper userMapper, ReservationService reservationService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.reservationService = reservationService;
    }

    /**
     * Retrieves a user based on its email address.
     *
     * @param email The email address of the user.
     * @return The User entity with the specified email address.
     * @throws ResourceNotFoundException if the user with the specified email address is not found.
     */
    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.USER_NOT_FOUND_MESSAGE, email)));
        return user;
    }

    /**
     * Saves a new user based on the registration request.
     *
     * @param registerRequest The registration request containing user details.
     * @throws ConflictException if a user with the provided email already exists.
     */
    public void saveUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException(String.format(ErrorMessage.EMAIL_ALREADY_EXIST_MESSAGE,
                    registerRequest.getEmail()));

        }
        Role role = roleService.findByType(RoleType.ROLE_CUSTOMER);

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(encodedPassword);
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setAddress(registerRequest.getAddress());
        user.setZipCode(registerRequest.getZipCode());
        user.setRoles(roles);

        userRepository.save(user);

    }

    /**
     * Retrieves a list of all users and maps them to UserDTO objects.
     *
     * @return List of UserDTO representing all users.
     */
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = userMapper.map(users);

        return userDTOs;
    }

    /**
     * Retrieves the currently authenticated user and maps it to a UserDTO object.
     *
     * @return UserDTO representing the currently authenticated user.
     */
    public UserDTO getPrincipal() {
        User currentUser = getCurrentUser();
        UserDTO userDTO = userMapper.userToUserDTO(currentUser);

        return userDTO;
    }

    /**
     * Retrieves the currently authenticated user based on the user's email.
     *
     * @return User representing the currently authenticated user.
     * @throws ResourceNotFoundException if the current user cannot be found.
     */
    public User getCurrentUser() {
        String email = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                new ResourceNotFoundException(ErrorMessage.PRINCIPAL_FOUND_MESSAGE));
        User user = getUserByEmail(email);
        return user;
    }

    /**
     * Retrieves a page of users with pagination.
     *
     * @param pageable The pagination information.
     * @return Page of UserDTO representing users.
     */
    public Page<UserDTO> getUserPage(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);

        return getUserDTOPage(userPage);

    }

    /**
     * Converts a Page<User> to a Page<UserDTO>.
     *
     * @param userPage The page of users.
     * @return Page of UserDTO representing users.
     */
    private Page<UserDTO> getUserDTOPage(Page<User> userPage) {
        Page<UserDTO> userDTOPage = userPage.map(new Function<User, UserDTO>() {
            @Override
            public UserDTO apply(User user) {
                return userMapper.userToUserDTO(user);
            }
        });
        return userDTOPage;
    }

    /**
     * Retrieves a User by ID and converts it to a UserDTO.
     *
     * @param id The ID of the user to retrieve.
     * @return UserDTO representing the retrieved user.
     * @throws ResourceNotFoundException if the user with the specified ID is not found.
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return userMapper.userToUserDTO(user);
    }

    /**
     * Updates the password for the currently authenticated user.
     *
     * @param updatePasswordRequest Object containing old and new password details.
     * @throws BadRequestException if the user is a built-in user or if the old password doesn't match the current password.
     */
    public void updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        User user = getCurrentUser();

        if (user.getBuiltin()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        if (!passwordEncoder.matches(updatePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorMessage.PASSWORD_NOT_MATCHED);
        }
        String hashedPassword = passwordEncoder.encode(updatePasswordRequest.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);

    }

    /**
     * Updates user information for the currently authenticated user.
     *
     * @param userUpdateRequest Object containing updated user details.
     * @throws BadRequestException if the user is a built-in user.
     * @throws ConflictException  if the provided email already exists for another user.
     */
    @Transactional
    public void updateUser(UserUpdateRequest userUpdateRequest) {
        User user = getCurrentUser();
        if (user.getBuiltin()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        boolean emailExist = userRepository.existsByEmail(userUpdateRequest.getEmail());
        if (emailExist && !userUpdateRequest.getEmail().equals(user.getEmail())) {
            throw new ConflictException(String.format(ErrorMessage.
                    EMAIL_ALREADY_EXIST_MESSAGE, userUpdateRequest.getEmail()));
        }
        userRepository.update(user.getId(), userUpdateRequest.getFirstName(),
                userUpdateRequest.getLastName(),
                userUpdateRequest.getPhoneNumber(),
                userUpdateRequest.getEmail(),
                userUpdateRequest.getAddress(),
                userUpdateRequest.getZipCode());

    }

    /**
     * Updates user information and roles for the specified user by an admin.
     *
     * @param id                     ID of the user to be updated.
     * @param adminUserUpdateRequest Object containing updated user details and roles.
     * @throws BadRequestException  if the user is built-in.
     * @throws ConflictException    if the provided email already exists for another user.
     */
    public void updateUserAuth(Long id, AdminUserUpdateRequest adminUserUpdateRequest) {

        User user = getById(id);
        if (user.getBuiltin()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        boolean emailExist = userRepository.existsByEmail(adminUserUpdateRequest.getEmail());
        if (emailExist && !adminUserUpdateRequest.getEmail().equals(user.getEmail())) {
            throw new ConflictException(String.format(ErrorMessage.
                    EMAIL_ALREADY_EXIST_MESSAGE, adminUserUpdateRequest.getEmail()));
        }
        if (adminUserUpdateRequest.getPassword() == null) {
            adminUserUpdateRequest.setPassword(user.getPassword());
        } else {
            String encodePassword = passwordEncoder.encode(adminUserUpdateRequest.getPassword());
        }
        Set<String> userStrRoles = adminUserUpdateRequest.getRoles();
        Set<Role> roles = convertRoles(userStrRoles);

        user.setFirstName(adminUserUpdateRequest.getFirstName());
        user.setLastName(adminUserUpdateRequest.getLastName());
        user.setPassword(adminUserUpdateRequest.getPassword());
        user.setEmail(adminUserUpdateRequest.getEmail());
        user.setPhoneNumber(user.getPhoneNumber());
        user.setAddress(adminUserUpdateRequest.getAddress());
        user.setBuiltin(adminUserUpdateRequest.getBuiltin());
        user.setZipCode(adminUserUpdateRequest.getZipCode());

        user.setRoles(roles);

        userRepository.save(user);

    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id ID of the user to be retrieved.
     * @return The user with the specified ID.
     * @throws ResourceNotFoundException if no user is found with the given ID.
     */
    public User getById(Long id) {

        User user = userRepository.findUserById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return user;
    }

    /**
     * Converts a set of role names to a set of Role objects.
     *
     * @param pRoles Set of role names to be converted.
     * @return A set of Role objects based on the given role names.
     */
    public Set<Role> convertRoles(Set<String> pRoles) {
        Set<Role> roles = new HashSet<>();
        if (pRoles == null) {
            Role userRole = roleService.findByType(RoleType.ROLE_CUSTOMER);
            roles.add(userRole);
        } else {
            pRoles.forEach(roleStr -> {
                if (roleStr.equals(RoleType.ROLE_ADMIN.getName())) {
                    Role adminRole = roleService.findByType(RoleType.ROLE_ADMIN);
                    roles.add(adminRole);
                } else {
                    Role userRole = roleService.findByType(RoleType.ROLE_CUSTOMER);
                    roles.add(userRole);
                }
            });
        }
        return roles;
    }

    /**
     * Removes a user by their ID.
     *
     * @param id ID of the user to be removed.
     * @throws BadRequestException If the user is a built-in user or if there are existing reservations associated with the user.
     */
    public void removeUserById(Long id) {
        User user = getById((id));

        if (user.getBuiltin()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        boolean exist = reservationService.existByUser(user);

        if (exist) {
            throw new BadRequestException(ErrorMessage.CAR_USED_BY_RESERVATION_MESSAGE);
        }

        userRepository.deleteById(id);

    }

    /**
     * Retrieves a list of all users.
     *
     * @return List of all users.
     */
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
