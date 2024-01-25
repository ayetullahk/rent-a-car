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

    /**
     * Retrieves a role based on its type.
     *
     * @param roleType The type of the role.
     * @return The Role entity with the specified type.
     * @throws ResourceNotFoundException if the role with the specified type is not found.
     */
    public Role findByType(RoleType roleType) {
        Role role = roleRepository.findByType(roleType).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessage.ROLE_NOT_FOUND_MESSAGE, roleType.name())));
        return role;
    }
}
