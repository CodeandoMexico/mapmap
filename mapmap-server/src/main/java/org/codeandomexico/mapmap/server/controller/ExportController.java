package org.codeandomexico.mapmap.server.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.codeandomexico.mapmap.server.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

@Controller
public class ExportController {

    private final ExportService exportService;

    private final String TIMESTAMP_FORMAT = "yyyyMMdd_hhmmssSSS";

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping(path = "/export-shapefile", produces = "application/zip")
    @ResponseBody
    public FileSystemResource exportShapefile(
            @RequestParam(name = "unitIds") Set<String> unitIds,
            HttpServletResponse response
    ) {
        String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
        Optional<File> exportFile = exportService.generateExportShapefile(unitIds, timestamp);
        response.setHeader("Content-Disposition", String.format("inline; filename=\"%s_SHP\"", timestamp));
        return exportFile.map(FileSystemResource::new).orElseGet(() -> null);
    }

    @GetMapping(path = "/export-csv", produces = "application/zip")
    @ResponseBody
    public FileSystemResource exportCsv(
            @RequestParam(name = "unitIds") Set<String> unitIds,
            HttpServletResponse response
    ) {
        String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
        Optional<File> exportFile = exportService.generateExportCsv(unitIds, timestamp);
        response.setHeader("Content-Disposition", String.format("inline; filename=\"%s_CSV\"", timestamp));
        return exportFile.map(FileSystemResource::new).orElseGet(() -> null);
    }

}
