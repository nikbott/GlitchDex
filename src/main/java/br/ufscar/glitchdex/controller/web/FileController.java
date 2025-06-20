package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private final FileStorageService fileStorageService;


    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        log.info("Request to serve file with filename: {}", filename);
        Resource file = fileStorageService.loadAsResource(filename);
        return ResponseEntity.ok()
                .body(file);
    }
}