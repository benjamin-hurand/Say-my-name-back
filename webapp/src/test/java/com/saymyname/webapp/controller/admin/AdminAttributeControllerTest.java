package com.saymyname.webapp.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.service.AttributeEnumOptionService;
import com.saymyname.service.AttributeService;
import com.saymyname.webapp.mapper.AttributeDtoMapper;
import com.saymyname.webapp.mapper.AttributeEnumOptionDtoMapper;
import com.saymyname.webapp.mapper.admin.AdminAttributeMutationDtoMapper;

@ExtendWith(MockitoExtension.class)
class AdminAttributeControllerTest {

    @Mock
    private AttributeService attributeService;
    @Mock
    private AttributeEnumOptionService optionService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminAttributeController controller = new AdminAttributeController(
                attributeService,
                optionService,
                new AttributeDtoMapper(),
                new AttributeEnumOptionDtoMapper(),
                new AdminAttributeMutationDtoMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void exposesAdminOwnerAuthorization() {
        PreAuthorize annotation = AdminAttributeController.class.getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value())
                .isEqualTo("@orgSecurity.hasRole(null, 'ADMIN') or @orgSecurity.hasRole(null, 'OWNER')");
    }

    @Test
    void createsAttribute() throws Exception {
        when(attributeService.create(any(Attribute.class), eq(List.of("Marketing", "Produit"))))
                .thenAnswer(invocation -> {
                    Attribute attribute = invocation.getArgument(0);
                    attribute.setId(42L);
                    return attribute;
                });
        when(optionService.getActiveOptionsByAttributeId(42L)).thenReturn(List.of());

        mockMvc.perform(post("/api/admin/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Département",
                                  "type": "ENUM",
                                  "maxValues": 1,
                                  "identitySource": false,
                                  "enumOptions": ["Marketing", "Produit"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/attributes/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("Département"));
    }

    @Test
    void updatesAttributeUsingPathId() throws Exception {
        when(attributeService.update(any(Attribute.class), eq(List.of())))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(optionService.getActiveOptionsByAttributeId(42L)).thenReturn(List.of());

        mockMvc.perform(put("/api/admin/attributes/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Bureau",
                                  "type": "TEXT",
                                  "maxValues": 1,
                                  "enumOptions": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.name").value("Bureau"));
    }

    @Test
    void deletesAttribute() throws Exception {
        mockMvc.perform(delete("/api/admin/attributes/42"))
                .andExpect(status().isNoContent());

        verify(attributeService).delete(42L);
    }

    @Test
    void reordersAttributes() throws Exception {
        mockMvc.perform(patch("/api/admin/attributes/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"id": 11, "displayOrder": 20},
                                  {"id": 12, "displayOrder": 10}
                                ]
                                """))
                .andExpect(status().isNoContent());

        verify(attributeService).reorder(List.of(
                new AttributeService.OrderUpdate(11L, 20),
                new AttributeService.OrderUpdate(12L, 10)));
    }
}
