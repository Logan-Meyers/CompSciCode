package org.cpts422.parking.controllers;

import org.cpts422.parking.entities.Citation;
import org.cpts422.parking.entities.User;
import org.cpts422.parking.exceptions.CitationAlreadyPaidException;
import org.cpts422.parking.exceptions.CitationNotFoundException;
import org.cpts422.parking.exceptions.PaymentDeclinedException;
import org.cpts422.parking.exceptions.PermitNotAllowedException;
import org.cpts422.parking.exceptions.UserNotFoundException;
import org.cpts422.parking.services.CitationService;
import org.cpts422.parking.services.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Officer page: issues a citation to the user registered to a license plate. */
@Controller
public class OfficerController {

    private final UserService userService;
    private final CitationService citationService;

    public OfficerController(UserService userService, CitationService citationService) {
        this.userService = userService;
        this.citationService = citationService;
    }

    @GetMapping("/officer")
    public String officer() {
        return "officer";
    }

    @PostMapping("/officer/citations")
    public String issueCitation(@RequestParam String licensePlate, RedirectAttributes redirect) {
        try {
            User user = userService.findByLicensePlate(licensePlate.trim());
            Citation citation = citationService.issueCitation(user.getId());
            redirect.addFlashAttribute("success", "Citation " + citation.getId() + " issued to "
                    + user.getName() + " (plate " + user.getLicensePlate() + ").");
        } catch (UserNotFoundException | PermitNotAllowedException | CitationNotFoundException
                 | CitationAlreadyPaidException | PaymentDeclinedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/officer";
    }
}
