package org.codeandomexico.mapmap.server.controller;

import org.codeandomexico.mapmap.server.model.Phone;
import org.codeandomexico.mapmap.server.model.TripPattern;
import org.codeandomexico.mapmap.server.repository.PhoneRepository;
import org.codeandomexico.mapmap.server.repository.TripPatternRepository;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;


@Controller
public class MainController {

    private final PhoneRepository phoneRepository;

    private final TripPatternRepository tripPatternRepository;

    Logger logger = LoggerFactory.getLogger(MainController.class);

    public MainController(PhoneRepository phoneRepository, TripPatternRepository tripPatternRepository) {
        this.phoneRepository = phoneRepository;
        this.tripPatternRepository = tripPatternRepository;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(name = "error", required = false) String error,
            @NotNull Model model
    ) {
        model.addAttribute("error", error);
        return "index";
    }

    @GetMapping("/view-unit")
    public String view(
            @RequestParam(name = "unitId", required = false) String unitId,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (unitId == null) {
            redirectAttributes.addAttribute("error", "Debe especificarse el ID de la unidad.");
            return "redirect:/";
        }
        Optional<Phone> phone = phoneRepository.findByUnitId(unitId);
        if (phone.isEmpty()) {
            redirectAttributes.addAttribute("error", "No existe el ID de la unidad.");
            return "redirect:/";
        }

        List<TripPattern> tripPatterns = tripPatternRepository.findByRoute_PhoneOrderByIdDesc(phone.get());
        model.addAttribute("phone", phone.get());
        model.addAttribute("tripPatterns", tripPatterns);
        return "view";
    }
}
