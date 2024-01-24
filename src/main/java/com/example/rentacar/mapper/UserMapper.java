package com.example.rentacar.mapper;

import com.example.rentacar.domain.User;
import com.example.rentacar.dto.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO userToUserDTO(User user);

    List<UserDTO> map(List<User> userList);

}
