package com.example.rentacar.repository;

import com.example.rentacar.domain.Role;
import com.example.rentacar.domain.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Integer> {

    Optional<Role> findByType(RoleType type);
}
