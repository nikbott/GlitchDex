package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.config.Constants;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
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
     * Landing page for anonymous users, redirects to dashboard if logged in.
     */
    @GetMapping({"/", "/home"})
    public String landingPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        log.info("Showing landing page for anonymous user");
        return "landing";
    }

    /**
     * Dashboard page for authenticated users.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, UserDTO user,
                            @RequestParam(name = Constants.SORT, defaultValue = "name") String sort,
                            @RequestParam(name = Constants.ORDER, defaultValue = "asc") String order) {
        if (user != null) {
            log.info("User {} is accessing dashboard page", user.getEmail());
            model.addAttribute("user", user);
            model.addAttribute("projects", projectService.findByMember(user, sort, order));
        } else {
            // Should be handled by security config, but as a fallback
            return "redirect:/login";
        }
        return "home"; // The home.html template is now the dashboard
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
