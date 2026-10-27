package edu.wsu.cpts422.reccenter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class PaymentController {

    static final List<String> STATES = List.of(
            "AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "GA", "HI", "IA",
            "ID", "IL", "IN", "KS", "KY", "LA", "MA", "MD", "ME", "MI", "MN", "MO", "MS",
            "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV", "NY", "OH", "OK", "OR", "PA",
            "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY");

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/")
    public String home(Model model) {
        if (!model.containsAttribute("paymentRequest")) {
            model.addAttribute("paymentRequest", new PaymentRequest());
        }
        addPageData(model);
        return "index";
    }

    @PostMapping("/payments")
    public String pay(@ModelAttribute PaymentRequest paymentRequest, BindingResult binding,
                      Model model, RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            model.addAttribute("error", "Please check the form values");
            addPageData(model);
            return "index";
        }
        try {
            Payment payment = paymentService.pay(paymentRequest);
            redirect.addFlashAttribute("message",
                    "Payment recorded. Receipt #" + payment.getReceiptNumber());
            return "redirect:/";
        } catch (PaymentException e) {
            model.addAttribute("error", e.getMessage());
            addPageData(model);
            return "index";
        }
    }

    private void addPageData(Model model) {
        model.addAttribute("payments", paymentService.history());
        model.addAttribute("states", STATES);
        model.addAttribute("plans", Plan.values());
        model.addAttribute("methods", PaymentMethod.values());
    }
}
