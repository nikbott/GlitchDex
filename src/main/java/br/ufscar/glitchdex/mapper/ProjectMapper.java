package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.ProjectRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(source = "members", target = "memberIds", qualifiedByName = "usersToIds")
    ProjectDTO toProjectDTO(Project project);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Project toProject(ProjectRequest projectRequest);

    List<ProjectDTO> toProjectDTOs(List<Project> projects);

    @Named("usersToIds")
    default List<Long> usersToIds(List<User> users) {
        if (null == users) {
            return null;
        }
        return users.stream().map(User::getId).collect(Collectors.toList());
    }
}