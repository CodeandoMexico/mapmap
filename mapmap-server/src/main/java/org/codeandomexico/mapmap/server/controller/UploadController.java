package org.codeandomexico.mapmap.server.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.codeandomexico.mapmap.server.model.Phone;
import org.codeandomexico.mapmap.server.service.MainService;
import org.codeandomexico.mapmap.server.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
public class UploadController {

	@Autowired
	private MainService mainService;

	@Autowired
	private UploadService uploadService;

	@PostMapping("/upload")
	public void uploadRoutes(@RequestParam(name = "imei") String imei, @RequestParam(name = "data") MultipartFile data,
			HttpServletResponse response) throws IOException {

		Optional<Phone> phone = mainService.loadPhone(imei);
		if (phone.isEmpty()) {
			response.sendError(HttpStatus.BAD_REQUEST.value());
			return;
		}
		if (uploadService.uploadRoutes(data, phone.get())) {
			response.sendError(HttpStatus.OK.value());
			return;
		}
		response.sendError(HttpStatus.BAD_REQUEST.value());
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Error en la peticion.")
	@ExceptionHandler(IOException.class)
	public void ioException() {
	}

}
