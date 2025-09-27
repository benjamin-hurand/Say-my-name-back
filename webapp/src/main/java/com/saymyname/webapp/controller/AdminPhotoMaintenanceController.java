// src/main/java/com/saymyname/webapp/controller/AdminPhotoMaintenanceController.java
package com.saymyname.webapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.saymyname.service.profile.PhotoBackfillService;

@RestController
@RequestMapping("/api/admin/photos")
public class AdminPhotoMaintenanceController {

    private final PhotoBackfillService backfill;

    public AdminPhotoMaintenanceController(PhotoBackfillService backfill) {
        this.backfill = backfill;
    }

    /**
     * POST /api/admin/photos/thumbnails:backfill?approvedOnly=true|false
     */
    @PostMapping("/thumbnails:backfill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> backfill(
            @RequestParam(name = "approvedOnly", defaultValue = "true") boolean approvedOnly) {
        String stats = approvedOnly
                ? backfill.backfillApprovedThumbnails()
                : backfill.backfillAllThumbnails();
        return ResponseEntity.ok(stats);
    }
}
