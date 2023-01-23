package org.codeandomexico.mapmap.server.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.codeandomexico.mapmap.server.model.Phone;
import org.codeandomexico.mapmap.server.model.TripPattern;
import org.codeandomexico.mapmap.server.repository.PhoneRepository;
import org.codeandomexico.mapmap.server.repository.TripPatternRepository;
import org.codeandomexico.mapmap.server.service.MainService;
import org.codeandomexico.mapmap.server.util.TripPatternListSerializer;
import org.codeandomexico.mapmap.server.util.TripPatternSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class APIController {

    @Autowired
    private MainService mainService;

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private TripPatternRepository tripPatternRepository;

    Logger logger = LoggerFactory.getLogger(APIController.class);

    @GetMapping(path = "/register", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Phone> register(
            @RequestParam(name = "imei", required = false) String imei, String userName
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        if (imei == null) {
            logger.error("El IMEI es null");
            return new ResponseEntity<>(null, responseHeaders, HttpStatus.BAD_REQUEST);
        }
        String _userName = userName != null ? userName.toUpperCase() : "Anónimo";
        Optional<Phone> phone = mainService.registerPhone(imei, _userName);
        return phone.map(value -> new ResponseEntity<>(value, responseHeaders, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(null, responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @GetMapping(path = "/list", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> list(
            @RequestParam(name = "unitId", required = false) String unitId
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        if (unitId == null || unitId.isEmpty()) {
            return new ResponseEntity<>(new JsonObject().toString(), responseHeaders, HttpStatus.BAD_REQUEST);
        }
        Optional<Phone> phone = phoneRepository.findFirstByUnitId(unitId);
        if (phone.isEmpty()) {
            return new ResponseEntity<>(new JsonObject().toString(), responseHeaders, HttpStatus.OK);
        }
        List<TripPattern> tripPatterns = tripPatternRepository.findByRoute_PhoneOrderByIdDesc(phone.get());
        Gson gson = new GsonBuilder().registerTypeAdapter(TripPattern.class, new TripPatternListSerializer()).serializeSpecialFloatingPointValues().serializeNulls().create();
        return new ResponseEntity<>(gson.toJson(tripPatterns), responseHeaders, HttpStatus.OK);
    }

    @GetMapping(path = "/pattern", produces = "application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<String> pattern(
            @RequestParam(name = "patternId", required = true, defaultValue = "0") Long patternId
    ) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        Optional<TripPattern> tripPattern = tripPatternRepository.findById(patternId);
        if (tripPattern.isPresent()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(TripPattern.class, new TripPatternSerializer()).serializeSpecialFloatingPointValues().serializeNulls().create();
            return new ResponseEntity<>(gson.toJson(tripPattern.orElse(null)), responseHeaders, HttpStatus.OK);
        }
        JsonObject error = new JsonObject();
        error.addProperty("error", "No existe ese Trip Pattern");
        return new ResponseEntity<>(error.toString(), responseHeaders, HttpStatus.NOT_FOUND);
    }
}
