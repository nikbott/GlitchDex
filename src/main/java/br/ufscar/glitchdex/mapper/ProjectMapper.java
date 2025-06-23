package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.ProjectRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ProjectMapper {

    ProjectDTO toProjectDTO(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toProject(ProjectRequest projectRequest);

    @Mapping(source = "members", target = "memberIds")
    ProjectRequest toProjectRequest(ProjectDTO projectDto);

    List<ProjectDTO> toProjectDTOs(List<Project> projects);

    default List<Long> membersToMemberIds(List<UserDTO> members) {
        if (members == null) {
            return null;
        }
        return members.stream().map(UserDTO::getId).collect(Collectors.toList());
    }
}