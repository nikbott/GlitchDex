package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.TestSession;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestSessionMapper {

    @Mapping(source = "strategy.id", target = "strategyId")
    @Mapping(source = "tester.id", target = "testerId")
    @Mapping(source = "tester.name", target = "testerName")
    TestSessionDTO toTestSessionDTO(TestSession testSession);

    List<TestSessionDTO> toTestSessionDTOs(List<TestSession> testSessions);
}