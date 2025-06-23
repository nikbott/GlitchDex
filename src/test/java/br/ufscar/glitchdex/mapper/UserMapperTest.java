package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.Role;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.dto.UserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Mapper Tests")
class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    @DisplayName("Should map User to UserDTO")
    void shouldMapUserToUserDTO() {
        // given
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(Role.TESTER);

        // when
        UserDTO userDTO = mapper.toUserDTO(user);

        // then
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getId()).isEqualTo(user.getId());
        assertThat(userDTO.getName()).isEqualTo(user.getName());
        assertThat(userDTO.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDTO.getRole()).isEqualTo(user.getRole());
    }

    @Test
    @DisplayName("Should map UserRequest to User")
    void shouldMapUserRequestToUser() {
        // given
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Test User");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("password");
        userRequest.setRole(Role.TESTER);

        // when
        User user = mapper.toUser(userRequest);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(userRequest.getName());
        assertThat(user.getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(user.getRole()).isEqualTo(userRequest.getRole());
        assertThat(user.getId()).isNull();
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
        assertThat(user.getTestSessions()).isEmpty();
    }

    @Test
    @DisplayName("Should map UserDTO to UserRequest")
    void shouldMapUserDTOToUserRequest() {
        // given
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("Test User");
        userDTO.setEmail("test@example.com");
        userDTO.setRole(Role.TESTER);

        // when
        UserRequest userRequest = mapper.toUserRequest(userDTO);

        // then
        assertThat(userRequest).isNotNull();
        assertThat(userRequest.getName()).isEqualTo(userDTO.getName());
        assertThat(userRequest.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(userRequest.getRole()).isEqualTo(userDTO.getRole());
        assertThat(userRequest.getPassword()).isNull();
    }
}