package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.Bug;
import br.ufscar.glitchdex.dto.BugDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BugMapper {

    @Mapping(source = "testSession.id", target = "testSessionId")
    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reporter.name", target = "reporterName")
    BugDTO toBugDTO(Bug bug);

    List<BugDTO> toBugDTOs(List<Bug> bugs);
}