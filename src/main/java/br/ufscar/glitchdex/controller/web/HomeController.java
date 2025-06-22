package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.config.Constants;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final ProjectService projectService;

    /**
     * Home page
     */
    @GetMapping("/")
    public String index() {
        log.info("Redirecting to home page");
        return "redirect:/home";
    }

    /**
     * Home page with dashboard
     */
    @GetMapping("/home")
    public String home(Model model, UserDTO user,
                       @RequestParam(name = Constants.SORT, defaultValue = "name") String sort,
                       @RequestParam(name = Constants.ORDER, defaultValue = "asc") String order) {
        if (user != null) {
            log.info("User {} is accessing home page", user.getEmail());
            model.addAttribute("user", user);
            model.addAttribute("projects", projectService.findByMember(user, sort, order));
        } else {
            log.info("Anonymous user is accessing home page");
        }


        return "home";
    }

    /**
     * Login page
     */
    @GetMapping("/login")
    public String login() {
        log.info("Request to show login page");
        return "login";
    }

    /**
     * Error page
     */
    @GetMapping("/error")
    public String error() {
        log.error("Request to error page");
        return "error";
    }
}