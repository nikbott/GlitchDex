package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.BugAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BugAttachmentRepository extends JpaRepository<BugAttachment, Long> {
}