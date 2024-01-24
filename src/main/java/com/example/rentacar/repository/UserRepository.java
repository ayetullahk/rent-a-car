package com.example.rentacar.repository;

import com.example.rentacar.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Boolean existsByEmail(String email);


    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = "roles")
    List<User>findAll();

    @EntityGraph(attributePaths = "roles")
    Page<User>findAll(Pageable pageable);

    @EntityGraph(attributePaths = "roles")
    Optional<User>findById(Long id);

    //parantez id belirtilmesi sonucu roller gelmez
    //bu durumda yazlızca userlar gelecektir
    @EntityGraph(attributePaths = "id")
    Optional<User>findUserById(Long id);

    @Modifying
    @Query("update User u set u.firstName=:firstName, u.lastName=:lastName,u.phoneNumber=:phoneNumber,u.email=:email,u.address=:address,u.zipCode=:zipCode where u.id=:id")
    void update(@Param("id") Long id,
                @Param("firstName") String firstName,
                @Param("lastName") String lastName,
                @Param("phoneNumber")String phoneNumber,
                @Param("email")String email,
                @Param("address")String address,
                @Param("zipCode")String zipCode);


}
