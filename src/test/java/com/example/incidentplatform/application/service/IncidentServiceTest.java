package com.example.incidentplatform.application.service;

import com.example.incidentplatform.application.port.IncidentRepository;
import com.example.incidentplatform.common.error.NotFoundException;
import com.example.incidentplatform.domain.model.incident.Incident;
import com.example.incidentplatform.domain.model.incident.IncidentStatus;
import com.example.incidentplatform.domain.model.incident.Severity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

        @Mock
        private IncidentRepository incidentRepository;

        private IncidentService incidentService;

        @BeforeEach
        void setup() {
                incidentService = new IncidentService(incidentRepository);
        }

        @Test
        @DisplayName("createIncident saves and returns new incident")
        void createIncident_savesAndReturnsIncident() {
                UUID tenantId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                String title = "Database Connection Failed";
                String description = "Connection pool exhausted";
                Severity severity = Severity.CRITICAL;

                var incident = Incident.createNew(tenantId, title, description, severity, userId);
                when(incidentRepository.save(any(Incident.class))).thenReturn(incident);

                var result = incidentService.createIncident(tenantId, title, description, severity, userId);

                assertNotNull(result);
                assertEquals(title, result.title());
                assertEquals(severity, result.severity());
                assertEquals(IncidentStatus.OPEN, result.status());
                verify(incidentRepository).save(any(Incident.class));
        }

        @Test
        @DisplayName("getIncident retrieves incident by ID")
        void getIncident_retrievesIncidentById() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                var incident = Incident.createNew(tenantId, "Issue", "Description", Severity.HIGH, UUID.randomUUID());

                when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                                .thenReturn(Optional.of(incident));

                var result = incidentService.getIncident(tenantId, incidentId);

                assertNotNull(result);
                assertEquals(incident.id(), result.id());
        }

        @Test
        @DisplayName("getIncident throws NotFoundException when incident not found")
        void getIncident_throwsNotFoundException_whenNotFound() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();

                when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                                .thenReturn(Optional.empty());

                assertThrows(NotFoundException.class, () -> incidentService.getIncident(tenantId, incidentId));
        }

        @Test
        @DisplayName("listIncidents returns all incidents for tenant")
        void listIncidents_returnsAllIncidentsForTenant() {
                UUID tenantId = UUID.randomUUID();
                var incident1 = Incident.createNew(tenantId, "Issue 1", "Desc", Severity.HIGH, UUID.randomUUID());
                var incident2 = Incident.createNew(tenantId, "Issue 2", "Desc", Severity.LOW, UUID.randomUUID());

                when(incidentRepository.findByTenantId(tenantId))
                                .thenReturn(List.of(incident1, incident2));

                var result = incidentService.listIncidents(tenantId);

                assertEquals(2, result.size());
                verify(incidentRepository).findByTenantId(tenantId);
        }

        @Test
        @DisplayName("deleteIncident removes incident successfully")
        void deleteIncident_removesIncidentSuccessfully() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();

                when(incidentRepository.existsByIdAndTenantId(incidentId, tenantId)).thenReturn(true);

                incidentService.deleteIncident(tenantId, incidentId);

                verify(incidentRepository).deleteByIdAndTenantId(incidentId, tenantId);
        }

        @Test
        @DisplayName("deleteIncident throws NotFoundException when incident not found")
        void deleteIncident_throwsNotFoundException_whenNotFound() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();

                when(incidentRepository.existsByIdAndTenantId(incidentId, tenantId)).thenReturn(false);

                assertThrows(NotFoundException.class, () -> incidentService.deleteIncident(tenantId, incidentId));

                verify(incidentRepository, never()).deleteByIdAndTenantId(any(), any());
        }

        // ==================== Update Incident Tests ====================

        @Test
        @DisplayName("updateIncident changes status from OPEN to IN_PROGRESS")
        void updateIncident_changesStatus() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                var existing = Incident.createNew(tenantId, "Issue", "Desc", Severity.HIGH, UUID.randomUUID());

                when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                                .thenReturn(Optional.of(existing));
                when(incidentRepository.save(any(Incident.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                var result = incidentService.updateIncident(
                                tenantId, incidentId, null, null, null, IncidentStatus.IN_PROGRESS);

                assertEquals(IncidentStatus.IN_PROGRESS, result.status());
                assertEquals(existing.title(), result.title()); // unchanged
                verify(incidentRepository).save(any(Incident.class));
        }

        @Test
        @DisplayName("updateIncident changes multiple fields at once")
        void updateIncident_changesMultipleFields() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                var existing = Incident.createNew(tenantId, "Old Title", "Old Desc", Severity.LOW, UUID.randomUUID());

                when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                                .thenReturn(Optional.of(existing));
                when(incidentRepository.save(any(Incident.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                var result = incidentService.updateIncident(
                                tenantId, incidentId, "New Title", "New Description", Severity.CRITICAL,
                                IncidentStatus.IN_PROGRESS);

                assertEquals("New Title", result.title());
                assertEquals("New Description", result.description());
                assertEquals(Severity.CRITICAL, result.severity());
                assertEquals(IncidentStatus.IN_PROGRESS, result.status());
        }

        @Test
        @DisplayName("updateIncident sets resolvedAt when status changes to RESOLVED")
        void updateIncident_setsResolvedAt_whenResolved() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                var existing = Incident.createNew(tenantId, "Issue", "Desc", Severity.HIGH, UUID.randomUUID());

                when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                                .thenReturn(Optional.of(existing));
                when(incidentRepository.save(any(Incident.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                var result = incidentService.updateIncident(
                                tenantId, incidentId, null, null, null, IncidentStatus.RESOLVED);

                assertEquals(IncidentStatus.RESOLVED, result.status());
                assertNotNull(result.resolvedAt()); // resolvedAt should be set!
        }

        @Test
        @DisplayName("updateIncident throws NotFoundException when incident not found")
        void updateIncident_throwsNotFoundException() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();

                when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                                .thenReturn(Optional.empty());

                assertThrows(NotFoundException.class,
                                () -> incidentService.updateIncident(tenantId, incidentId, "New Title", null, null,
                                                null));
        }

        @Test
        @DisplayName("escalateIncident increases severity by one level")
        void escalateIncident_increasesSeverity() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                var existing = Incident.createNew(tenantId, "Issue", "Desc", Severity.MEDIUM, UUID.randomUUID());

                when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                                .thenReturn(Optional.of(existing));
                when(incidentRepository.save(any(Incident.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                var result = incidentService.escalateIncident(tenantId, incidentId);

                assertEquals(Severity.HIGH, result.severity()); // MEDIUM → HIGH
        }

        @Test
        @DisplayName("escalateIncident keeps CRITICAL severity when already at max")
        void escalateIncident_keepsMaxSeverity() {
                UUID tenantId = UUID.randomUUID();
                UUID incidentId = UUID.randomUUID();
                var existing = Incident.createNew(tenantId, "Issue", "Desc", Severity.CRITICAL, UUID.randomUUID());

                when(incidentRepository.findByIdAndTenantId(incidentId, tenantId))
                                .thenReturn(Optional.of(existing));
                when(incidentRepository.save(any(Incident.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                var result = incidentService.escalateIncident(tenantId, incidentId);

                assertEquals(Severity.CRITICAL, result.severity()); // stays CRITICAL
        }

        // ==================== Search Tests ====================

        @Test
        @DisplayName("searchIncidents returns filtered results")
        void searchIncidents_returnsFilteredResults() {
                UUID tenantId = UUID.randomUUID();
                var incident = Incident.createNew(tenantId, "Server Down", "Desc", Severity.HIGH, UUID.randomUUID());

                var criteria = new com.example.incidentplatform.api.dto.incident.IncidentSearchCriteria(
                                "server", IncidentStatus.OPEN, null, null, null, null, "createdAt", "DESC");

                when(incidentRepository.search(
                                eq(tenantId), eq("server"), eq("OPEN"), isNull(), isNull(), isNull(), isNull(),
                                eq("createdAt"), eq("DESC")))
                                .thenReturn(List.of(incident));

                var result = incidentService.searchIncidents(tenantId, criteria);

                assertEquals(1, result.size());
                assertEquals("Server Down", result.get(0).title());
        }

        @Test
        @DisplayName("listIncidentsBySeverity returns incidents with matching severity")
        void listIncidentsBySeverity_returnsMatchingIncidents() {
                UUID tenantId = UUID.randomUUID();
                var incident = Incident.createNew(tenantId, "Critical Issue", "Desc", Severity.CRITICAL,
                                UUID.randomUUID());

                when(incidentRepository.findByTenantIdAndSeverity(tenantId, "CRITICAL"))
                                .thenReturn(List.of(incident));

                var result = incidentService.listIncidentsBySeverity(tenantId, Severity.CRITICAL);

                assertEquals(1, result.size());
                assertEquals(Severity.CRITICAL, result.get(0).severity());
        }

        @Test
        @DisplayName("countIncidents returns total count for tenant")
        void countIncidents_returnsTotalCount() {
                UUID tenantId = UUID.randomUUID();
                when(incidentRepository.countByTenantId(tenantId)).thenReturn(10L);

                long count = incidentService.countIncidents(tenantId);

                assertEquals(10L, count);
        }

        @Test
        @DisplayName("countIncidentsByStatus returns count for specific status")
        void countIncidentsByStatus_returnsStatusCount() {
                UUID tenantId = UUID.randomUUID();
                when(incidentRepository.countByTenantIdAndStatus(tenantId, "OPEN")).thenReturn(5L);

                long count = incidentService.countIncidentsByStatus(tenantId, IncidentStatus.OPEN);

                assertEquals(5L, count);
        }
}
