package com.example.rentacar.service;

import com.example.rentacar.domain.Role;
import com.example.rentacar.domain.enums.RoleType;
import com.example.rentacar.exception.ResourceNotFoundException;
import com.example.rentacar.exception.message.ErrorMessage;
import com.example.rentacar.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role findByType(RoleType roleType) {
        Role role = roleRepository.findByType(roleType).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.ROLE_NOT_FOUND_MESSAGE, roleType.name())));
        return role;
    }
}
