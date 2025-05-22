package com.saymyname.webapp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/photo") // Common base path for photo-related operations
public class PhotoRestController {

    public PhotoRestController() {
    }

}
