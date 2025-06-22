package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.Bug;
import br.ufscar.glitchdex.domain.BugAttachment;
import br.ufscar.glitchdex.dto.BugDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BugMapper {

    @Mapping(source = "testSession.id", target = "testSessionId")
    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reporter.name", target = "reporterName")
    @Mapping(source = "attachments", target = "attachmentFilenames", qualifiedByName = "attachmentsToFilenames")
    BugDTO toBugDTO(Bug bug);

    List<BugDTO> toBugDTOs(List<Bug> bugs);

    @Named("attachmentsToFilenames")
    default Set<String> attachmentsToFilenames(Set<BugAttachment> attachments) {
        if (attachments == null) {
            return null;
        }
        return attachments.stream()
                .map(BugAttachment::getFilename)
                .collect(Collectors.toSet());
    }
}