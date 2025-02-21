package com.saymyname.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/photo")  // Common base path for photo-related operations
public class PhotoRestController {
    private static final Logger logger = LoggerFactory.getLogger(PhotoRestController.class);

    public PhotoRestController() {
    }

}

