package br.ufscar.glitchdex.mapper;

import br.ufscar.glitchdex.domain.Bug;
import br.ufscar.glitchdex.domain.BugAttachment; // Certifique-se de que está importado
import br.ufscar.glitchdex.dto.BugDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named; // Certifique-se de que está importado
import org.springframework.stereotype.Component; // Se não estiver já no Mapper, adicione

import java.util.Collections; // Importado para Collections.emptySet()
import java.util.List;
import java.util.Set; // Certifique-se de que está importado
import java.util.stream.Collectors; // Certifique-se de que está importado

@Mapper(componentModel = "spring")
// Se você não tem @Component aqui, MapStruct ainda vai gerar a implementação,
// mas se for usar injeção de dependência diretamente do mapper, é bom ter.
@Component // Adicione @Component se ainda não tiver e quiser que ele seja um bean Spring
public interface BugMapper {

    @Mapping(source = "testSession.id", target = "testSessionId")
    @Mapping(source = "reporter.id", target = "reporterId")
    @Mapping(source = "reporter.name", target = "reporterName")
    // >>>>> MUDANÇA CRUCIAL AQUI: Mapeando 'attachments' do domínio para 'attachments' do DTO
    // Usando o método qualificado 'mapAttachments' para criar os BugDTO.AttachmentDTOs
    @Mapping(source = "attachments", target = "attachments", qualifiedByName = "mapAttachments")
    BugDTO toBugDTO(Bug bug);

    List<BugDTO> toBugDTOs(List<Bug> bugs);

    // >>>>> NOVO/MODIFICADO MÉTODO QUALIFICADO: Renomeado e agora cria BugDTO.AttachmentDTO
    @Named("mapAttachments") // Nome do método qualificado que o @Mapping usa
    default Set<BugDTO.AttachmentDTO> mapAttachments(Set<BugAttachment> attachments) {
        if (attachments == null) {
            return Collections.emptySet(); // Retorna um Set vazio para evitar NullPointerException no Thymeleaf
        }
        return attachments.stream()
                .map(bugAttachment -> {
                    BugDTO.AttachmentDTO attachmentDTO = new BugDTO.AttachmentDTO();
                    attachmentDTO.setId(bugAttachment.getId());         // Copia o ID
                    attachmentDTO.setFilename(bugAttachment.getFilename()); // Copia o nome do arquivo
                    return attachmentDTO;
                })
                .collect(Collectors.toSet());
    }

    // O método 'attachmentsToFilenames' original pode ser removido, pois não é mais usado para mapear o DTO.
    // Se você ainda o usa em outro lugar, pode mantê-lo, mas ele não será usado pelo mapeamento principal.
    /*
    @Named("attachmentsToFilenames")
    default Set<String> attachmentsToFilenames(Set<BugAttachment> attachments) {
        if (attachments == null) {
            return null;
        }
        return attachments.stream()
                .map(BugAttachment::getFilename)
                .collect(Collectors.toSet());
    }
    */
}