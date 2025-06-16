package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.Strategy;
import br.ufscar.glitchdex.dto.StrategyDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StrategyMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "creator.id", target = "creatorId")
    StrategyDTO toStrategyDTO(Strategy strategy);

    List<StrategyDTO> toStrategyDTOs(List<Strategy> strategies);
}