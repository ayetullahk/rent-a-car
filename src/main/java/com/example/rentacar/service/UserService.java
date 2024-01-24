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

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.USER_NOT_FOUND_MESSAGE, email)));
        return user;
    }

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

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = userMapper.map(users);

        return userDTOs;
    }

    public UserDTO getPrincipal() {
        User currentUser = getCurrentUser();
        UserDTO userDTO = userMapper.userToUserDTO(currentUser);

        return userDTO;
    }

    public User getCurrentUser() {
        String email = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
                new ResourceNotFoundException(ErrorMessage.PRINCIPAL_FOUND_MESSAGE));
        User user = getUserByEmail(email);
        return user;
    }

    public Page<UserDTO> getUserPage(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);

        return getUserDTOPage(userPage);

    }

    private Page<UserDTO> getUserDTOPage(Page<User> userPage) {
        Page<UserDTO> userDTOPage = userPage.map(new Function<User, UserDTO>() {
            @Override
            public UserDTO apply(User user) {
                return userMapper.userToUserDTO(user);
            }
        });
        return userDTOPage;
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return userMapper.userToUserDTO(user);
    }

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

    public User getById(Long id) {

        User user = userRepository.findUserById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return user;
    }

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

    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
