package org.cpts422.parking.controllers;

import org.cpts422.parking.entities.Permit;
import org.cpts422.parking.entities.User;
import org.cpts422.parking.entities.Zone;
import org.cpts422.parking.exceptions.CitationAlreadyPaidException;
import org.cpts422.parking.exceptions.CitationNotFoundException;
import org.cpts422.parking.exceptions.PaymentDeclinedException;
import org.cpts422.parking.exceptions.PermitNotAllowedException;
import org.cpts422.parking.exceptions.UserNotFoundException;
import org.cpts422.parking.services.CitationService;
import org.cpts422.parking.services.PermitService;
import org.cpts422.parking.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** User page: shows a user's permit and citations, buys permits, pays citations. */
@Controller
public class UserController {

    private final UserService userService;
    private final PermitService permitService;
    private final CitationService citationService;

    public UserController(UserService userService,
                          PermitService permitService,
                          CitationService citationService) {
        this.userService = userService;
        this.permitService = permitService;
        this.citationService = citationService;
    }

    @GetMapping("/users/{userId}")
    public String showUser(@PathVariable Long userId, Model model, RedirectAttributes redirect) {
        try {
            User user = userService.getUser(userId);
            model.addAttribute("user", user);
            model.addAttribute("permit", permitService.findPermitForUser(userId).orElse(null));
            model.addAttribute("citations", citationService.findCitationsForUser(userId));
            model.addAttribute("zones", Zone.values());
            return "user";
        } catch (UserNotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/users/{userId}/permits")
    public String purchasePermit(@PathVariable Long userId, @RequestParam Zone zone,
                                 RedirectAttributes redirect) {
        try {
            Permit permit = permitService.purchasePermit(userId, zone);
            redirect.addFlashAttribute("success", "Permit purchased for the " + permit.getZone() + " zone.");
        } catch (UserNotFoundException | PermitNotAllowedException | CitationNotFoundException
                 | CitationAlreadyPaidException | PaymentDeclinedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/" + userId;
    }

    @PostMapping("/users/{userId}/citations/{citationId}/pay")
    public String payCitation(@PathVariable Long userId, @PathVariable Long citationId,
                              RedirectAttributes redirect) {
        try {
            citationService.payCitation(citationId);
            redirect.addFlashAttribute("success", "Citation " + citationId + " paid.");
        } catch (UserNotFoundException | PermitNotAllowedException | CitationNotFoundException
                 | CitationAlreadyPaidException | PaymentDeclinedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users/" + userId;
    }
}
