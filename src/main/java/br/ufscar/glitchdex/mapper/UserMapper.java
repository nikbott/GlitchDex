package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.dto.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toUserDTO(User user);

    List<UserDTO> toUserDTOs(List<User> users);

    @Mapping(target = "password", ignore = true)
    UserRequest toUserRequest(UserDTO userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "testSessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toUser(UserRequest userRequest);
}