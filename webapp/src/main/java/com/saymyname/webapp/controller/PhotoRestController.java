package com.saymyname.webapp.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/photo")  // Common base path for photo-related operations
public class PhotoRestController {
    private static final Logger logger = LogManager.getLogger(PhotoRestController.class);

    public PhotoRestController() {
    }

}

