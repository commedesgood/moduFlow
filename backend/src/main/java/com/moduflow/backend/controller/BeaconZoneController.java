package com.moduflow.backend.controller;

import com.moduflow.backend.dto.BeaconZoneCreateRequest;
import com.moduflow.backend.dto.BeaconZoneResponse;
import com.moduflow.backend.dto.BeaconZoneUpdateRequest;
import com.moduflow.backend.dto.OkResponse;
import com.moduflow.backend.service.BeaconZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/beacon-zones", "/api/v1/admin/beacon-zones"})
@Tag(name = "Beacon zones", description = "CMS beacon zone display name and capacity settings")
public class BeaconZoneController {

    private final BeaconZoneService beaconZoneService;

    public BeaconZoneController(BeaconZoneService beaconZoneService) {
        this.beaconZoneService = beaconZoneService;
    }

    @GetMapping
    @Operation(summary = "List beacon zones")
    public List<BeaconZoneResponse> getAll() {
        return beaconZoneService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create beacon zone")
    public BeaconZoneResponse create(@Valid @RequestBody BeaconZoneCreateRequest request) {
        return beaconZoneService.create(request);
    }

    @PutMapping("/{beaconId}")
    @Operation(summary = "Update beacon zone")
    public BeaconZoneResponse update(@PathVariable String beaconId,
                                     @Valid @RequestBody BeaconZoneUpdateRequest request) {
        return beaconZoneService.update(beaconId, request);
    }

    @DeleteMapping("/{beaconId}")
    @Operation(summary = "Delete beacon zone")
    public OkResponse delete(@PathVariable String beaconId) {
        beaconZoneService.delete(beaconId);
        return new OkResponse(true);
    }
}
