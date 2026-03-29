package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.AppointmentResponse;
import com.healthcare.doctor.dto.AuthResponse;
import com.healthcare.doctor.dto.DoctorActionRequest;
import com.healthcare.doctor.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Doctor Appointment Management
 * Provides endpoints for doctors to view and manage appointments
 * 
 * Base URL: /api/appointments
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * GET /api/appointments/pending
     * Get all pending appointments (awaiting doctor action)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getPendingAppointments(
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String doctorId = user.getId();
            List<AppointmentResponse> appointments = appointmentService.getPendingAppointments(doctorId, authorization);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/appointments/accepted
     * Get all accepted (confirmed) appointments
     */
    @GetMapping("/accepted")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getAcceptedAppointments(
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String doctorId = user.getId();
            List<AppointmentResponse> appointments = appointmentService.getAcceptedAppointments(doctorId, authorization);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/appointments
     * Get all appointments for the doctor (optionally filtered by status)
     */
    @GetMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String doctorId = user.getId();
            List<AppointmentResponse> appointments = appointmentService.getAppointmentsForDoctor(doctorId, status, authorization);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/appointments/{appointmentId}
     * Get a specific appointment details
     */
    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> getAppointmentDetails(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String doctorId = user.getId();
            AppointmentResponse appointment = appointmentService.getAppointment(doctorId, appointmentId, authorization);
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * POST /api/appointments/{appointmentId}/accept
     * Doctor accepts an appointment
     */
    @PostMapping("/{appointmentId}/accept")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> acceptAppointment(
            @PathVariable String appointmentId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        log.debug("Accept appointment called - appointmentId: {}, user: {}, authorization present: {}", 
                appointmentId, user, authorization != null);
        
        try {
            if (user == null) {
                log.warn("Accept appointment failed: user is null (authentication failed)");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            log.debug("User authenticated: id={}, username={}, roles: {}", 
                    user.getId(), user.getUsername(), user.getRoles());
            
            String doctorId = user.getId();
            String message = body != null ? body.get("message") : null;
            
            log.debug("Calling appointmentService.acceptAppointment for doctorId: {}", doctorId);
            AppointmentResponse result = appointmentService.acceptAppointment(doctorId, appointmentId, message, authorization);
            
            log.debug("Appointment accepted successfully");
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Error accepting appointment: {}", e.getMessage(), e);
            if (e.getMessage() != null && e.getMessage().contains("401")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Unexpected error accepting appointment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/appointments/{appointmentId}/decline
     * Doctor declines an appointment
     */
    @PostMapping("/{appointmentId}/decline")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> declineAppointment(
            @PathVariable String appointmentId,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String doctorId = user.getId();
            String reason = body != null ? body.get("reason") : null;
            AppointmentResponse result = appointmentService.declineAppointment(doctorId, appointmentId, reason, authorization);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * POST /api/appointments/{appointmentId}/action
     * Generic doctor action endpoint (ACCEPT, DECLINE, REQUEST_RESCHEDULE)
     * 
     * Request body example:
     * {
     *   "action": "ACCEPT" | "DECLINE" | "REQUEST_RESCHEDULE",
     *   "message": "optional message",
     *   "proposedStartTime": "2024-02-15T14:00:00",  (for REQUEST_RESCHEDULE)
     *   "proposedEndTime": "2024-02-15T14:30:00"     (for REQUEST_RESCHEDULE)
     * }
     */
    @PostMapping("/{appointmentId}/action")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> performAction(
            @PathVariable String appointmentId,
            @RequestBody DoctorActionRequest request,
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String doctorId = user.getId();
            AppointmentResponse result = appointmentServiceClient.performDoctorAction(doctorId, appointmentId, request, authorization);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private final com.healthcare.doctor.client.AppointmentServiceClient appointmentServiceClient;

    /**
     * POST /api/appointments/{appointmentId}/reschedule
     * Doctor requests appointment reschedule with proposed time
     */
    @PostMapping("/{appointmentId}/reschedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> requestReschedule(
            @PathVariable String appointmentId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String doctorId = user.getId();
            String message = (String) body.get("message");
            LocalDateTime proposedStartTime = LocalDateTime.parse((String) body.get("proposedStartTime"));
            LocalDateTime proposedEndTime = LocalDateTime.parse((String) body.get("proposedEndTime"));
            
            AppointmentResponse result = appointmentService.requestReschedule(
                    doctorId,
                    appointmentId,
                    message,
                    proposedStartTime,
                    proposedEndTime,
                    authorization);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PATCH /api/appointments/{appointmentId}/complete
     * Mark appointment as completed
     */
    @PatchMapping("/{appointmentId}/complete")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @PathVariable String appointmentId,
            @AuthenticationPrincipal AuthResponse.User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String doctorId = user.getId();
            AppointmentResponse result = appointmentService.completeAppointment(doctorId, appointmentId, authorization);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
