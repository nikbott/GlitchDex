package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.dto.BugDTO;
import br.ufscar.glitchdex.dto.BugRequest;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.service.BugService;
import br.ufscar.glitchdex.service.TestSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Adicionado para lidar com erros de validação
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/bugs")
@RequiredArgsConstructor
public class BugController {

    private static final Logger log = LoggerFactory.getLogger(BugController.class);
    private final BugService bugService;
    private final TestSessionService testSessionService;

    @GetMapping("/new")
    public String newBugForm(@RequestParam("testSessionId") Long testSessionId, Model model) {
        log.info("Request to show new bug form for test session with id: {}", testSessionId);
        BugRequest bugRequest = new BugRequest();
        bugRequest.setTestSessionId(testSessionId);
        model.addAttribute("bugRequest", bugRequest);
        return "bug/form";
    }

    @PostMapping
    public String createBug(@Valid @ModelAttribute("bugRequest") BugRequest bugRequest,
                            BindingResult result, // <-- ADICIONADO: Para capturar os resultados da validação
                            @RequestParam(value = "attachment", required = false) MultipartFile attachment, // <-- ALTERADO: 'required = false' para tornar o anexo opcional
                            UserDTO user) throws IllegalStatusChangeException {

        // <-- ADICIONADO: Bloco para verificar erros de validação
        if (result.hasErrors()) {
            log.warn("Validation errors occurred during bug creation for test session: {}", bugRequest.getTestSessionId());
            // Se houver erros, retorna para o formulário para que as mensagens de erro sejam exibidas
            // É importante adicionar o testSessionId de volta ao modelo para o link de 'Cancel' no formulário
            // model.addAttribute("testSessionId", bugRequest.getTestSessionId()); // Pode ser necessário se o form.html precisar do testSessionId diretamente
            return "bug/form";
        }

        log.info("User {} is creating a new bug for test session: {}", user.getEmail(), bugRequest.getTestSessionId());
        // Se a validação for bem-sucedida, prossegue com a criação do bug
        BugDTO createdBug = bugService.create(bugRequest, user, attachment);
        log.info("Bug created successfully with id: {}", createdBug.getId());

        // Redireciona para a página de detalhes do bug recém-criado
        return "redirect:/bugs/" + createdBug.getId();
    }

    @GetMapping("/{id}")
    public String viewBug(@PathVariable Long id, Model model) {
        log.info("Request to view bug with id: {}", id);
        BugDTO bug = bugService.findById(id);
        TestSessionDTO session = testSessionService.findById(bug.getTestSessionId());
        model.addAttribute("bug", bug);
        model.addAttribute("session", session);
        return "bug/view";
    }

    @PostMapping("/{id}/delete")
    public String deleteBug(@PathVariable Long id) {
        log.info("Request to delete bug with id: {}", id);
        Long testSessionId = bugService.delete(id);
        log.info("Bug with id: {} deleted successfully", id);
        return "redirect:/sessions/" + testSessionId;
    }
}