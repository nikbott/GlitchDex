package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.TestSession;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = BugMapper.class)
public interface TestSessionMapper {

    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "strategy.id", target = "strategyId")
    @Mapping(source = "tester.id", target = "testerId")
    @Mapping(source = "tester.name", target = "testerName")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "bugs", target = "bugs")
    TestSessionDTO toTestSessionDTO(TestSession testSession);

    List<TestSessionDTO> toTestSessionDTOs(List<TestSession> testSessions);

    TestSessionRequest toTestSessionRequest(TestSessionDTO testSessionDTO);
}