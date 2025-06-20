package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.Strategy;
import br.ufscar.glitchdex.dto.StrategyDTO;
import br.ufscar.glitchdex.dto.StrategyRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StrategyMapper {

    @Mapping(source = "creator.id", target = "creatorId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "examples", target = "examples")
    @Mapping(source = "tips", target = "tips")
    @Mapping(source = "imageUrl", target = "imageUrl")
    StrategyDTO toStrategyDTO(Strategy strategy);

    List<StrategyDTO> toStrategyDTOs(List<Strategy> strategies);

    StrategyRequest toStrategyRequest(StrategyDTO strategyDto);
}