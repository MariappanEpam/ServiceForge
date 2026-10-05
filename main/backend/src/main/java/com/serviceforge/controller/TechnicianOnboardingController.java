package com.serviceforge.controller;

import com.serviceforge.dto.ActivateTechnicianRequest;
import com.serviceforge.dto.ApiError;
import com.serviceforge.dto.CreateTechnicianRequest;
import com.serviceforge.dto.TechnicianInviteResponse;
import com.serviceforge.dto.TechnicianResponse;
import com.serviceforge.dto.UpdateTechnicianRequest;
import com.serviceforge.model.Technician;
import com.serviceforge.model.TechnicianStatus;
import com.serviceforge.persistence.ServiceForgeDataStore;
import com.serviceforge.service.RoleContext;
import com.serviceforge.service.TechnicianLifecycleService;
import com.serviceforge.service.TechnicianManagementService;
import com.serviceforge.service.TechnicianOnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianOnboardingController {

    private final RoleContext roleContext;
    private final ServiceForgeDataStore dataStore;
    private final TechnicianManagementService managementService;
    private final TechnicianOnboardingService onboardingService;
    private final TechnicianLifecycleService lifecycleService;

    public TechnicianOnboardingController(RoleContext roleContext,
                                         ServiceForgeDataStore dataStore,
                                         TechnicianManagementService managementService,
                                         TechnicianOnboardingService onboardingService,
                                         TechnicianLifecycleService lifecycleService) {
        this.roleContext = roleContext;
        this.dataStore = dataStore;
        this.managementService = managementService;
        this.onboardingService = onboardingService;
        this.lifecycleService = lifecycleService;
    }

    @PostMapping
    public ResponseEntity<?> createTechnician(
            @RequestHeader(value = "X-SF-Role", required = false) String roleHeader,
            @Valid @RequestBody CreateTechnicianRequest request) {

        RoleContext.Role role = roleContext.parseRole(roleHeader);
        roleContext.requireAdminOrDispatcher(role);

        // Create technician in the configured datastore.
        // In sqlite mode, IDs are assigned by the datastore.
        long id = dataStore.nextTechnicianId();
        Technician t = managementService.create(request, id);
        t = dataStore.createTechnician(t);

        // Feature 3: onboarding tokens are supported only for in-memory mock mode.
        // SQLite mode stores only the minimal technician fields and cannot persist lifecycle/token state.
        // NOTE: the injected bean is a @Primary ServiceForgeDataStore which may be a proxy; use class name
        // check instead of instanceof to avoid false negatives.
        String dsClass = dataStore.getClass().getName();
        if (!dsClass.contains("InMemoryDataStoreAdapter")) {
            throw new IllegalStateException("Technician onboarding is only supported in memory mode");
        }

        String token = onboardingService.generateAndIndexToken(t.getId());
        t.setInvitationToken(token);

        return ResponseEntity.status(201)
                .body(new TechnicianInviteResponse(TechnicianResponse.from(t), token));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTechnician(
            @RequestHeader(value = "X-SF-Role", required = false) String roleHeader,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTechnicianRequest request) {

        RoleContext.Role role = roleContext.parseRole(roleHeader);
        roleContext.requireAdminOrDispatcher(role);

        Technician t = managementService.getRequired(id);
        managementService.update(t, request);

        return ResponseEntity.ok(TechnicianResponse.from(t));
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@Valid @RequestBody ActivateTechnicianRequest request) {
        Technician t = onboardingService.activate(request);
        return ResponseEntity.ok(TechnicianResponse.from(t));
    }

    @PostMapping("/{id}/reinvite")
    public ResponseEntity<?> reinvite(
            @RequestHeader(value = "X-SF-Role", required = false) String roleHeader,
            @PathVariable Long id) {

        RoleContext.Role role = roleContext.parseRole(roleHeader);
        roleContext.requireAdminOrDispatcher(role);

        Technician t = managementService.getRequired(id);
        if (t.getStatus() == TechnicianStatus.OFFBOARDED) {
            return ResponseEntity.status(409)
                    .body(new ApiError(409, "CONFLICT", "Cannot reinvite an offboarded technician", "Cannot reinvite offboarded technician", null));
        }

        String token = onboardingService.generateAndIndexToken(t.getId());
        t.setInvitationToken(token);

        return ResponseEntity.ok(new TechnicianInviteResponse(TechnicianResponse.from(t), token));
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<?> suspend(
            @RequestHeader(value = "X-SF-Role", required = false) String roleHeader,
            @PathVariable Long id) {

        RoleContext.Role role = roleContext.parseRole(roleHeader);
        roleContext.requireAdminOrDispatcher(role);

        Technician t = managementService.getRequired(id);
        lifecycleService.transition(t, TechnicianStatus.SUSPENDED);
        return ResponseEntity.ok(TechnicianResponse.from(t));
    }

    @PostMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivate(
            @RequestHeader(value = "X-SF-Role", required = false) String roleHeader,
            @PathVariable Long id) {

        RoleContext.Role role = roleContext.parseRole(roleHeader);
        roleContext.requireAdminOrDispatcher(role);

        Technician t = managementService.getRequired(id);
        lifecycleService.transition(t, TechnicianStatus.ACTIVE);
        return ResponseEntity.ok(TechnicianResponse.from(t));
    }

    @PostMapping("/{id}/offboard")
    public ResponseEntity<?> offboard(
            @RequestHeader(value = "X-SF-Role", required = false) String roleHeader,
            @PathVariable Long id) {

        RoleContext.Role role = roleContext.parseRole(roleHeader);
        roleContext.requireAdminOrDispatcher(role);

        Technician t = managementService.getRequired(id);
        lifecycleService.transition(t, TechnicianStatus.OFFBOARDED);
        t.setInvitationToken(null);
        return ResponseEntity.ok(TechnicianResponse.from(t));
    }
}
