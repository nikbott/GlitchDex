package br.ufscar.glitchdex.controller;

import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.service.ProjectService;
import br.ufscar.glitchdex.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;
    private final ProjectService projectService;

    /**
     * Home page
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    /**
     * Home page with dashboard
     */
    @GetMapping("/home")
    public String home(Model model, Authentication authentication,
                       @RequestParam(name = "sort", defaultValue = "name") String sort,
                       @RequestParam(name = "order", defaultValue = "asc") String order) {
        if (null != authentication && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            model.addAttribute("user", user);
            model.addAttribute("projects", projectService.findByMember(user, sort, order));
        }

        return "home";
    }

    /**
     * Login page
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Error page
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
