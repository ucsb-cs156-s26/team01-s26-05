package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "SKY")
                .param("orgTranslationShort", "Skydiving Club")
                .param("orgTranslation", "UCSB Skydiving Club")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "SKY")
                .param("orgTranslationShort", "Skydiving Club")
                .param("orgTranslation", "UCSB Skydiving Club")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

    UCSBOrganization sky =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("Skydiving Club")
            .orgTranslation("UCSB Skydiving Club")
            .inactive(false)
            .build();

    UCSBOrganization chess =
        UCSBOrganization.builder()
            .orgCode("CHESS")
            .orgTranslationShort("Chess Club")
            .orgTranslation("UCSB Chess Club")
            .inactive(true)
            .build();

    ArrayList<UCSBOrganization> expectedOrgs = new ArrayList<>();
    expectedOrgs.addAll(Arrays.asList(sky, chess));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrgs);

    MvcResult response =
        mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isOk()).andReturn();

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrgs);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {

    UCSBOrganization sky =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("Skydiving Club")
            .orgTranslation("UCSB Skydiving Club")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(sky))).thenReturn(sky);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post")
                    .param("orgCode", "SKY")
                    .param("orgTranslationShort", "Skydiving Club")
                    .param("orgTranslation", "UCSB Skydiving Club")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).save(sky);
    String expectedJson = mapper.writeValueAsString(sky);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    UCSBOrganization sky =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("Skydiving Club")
            .orgTranslation("UCSB Skydiving Club")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(sky));

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization").param("orgCode", "SKY"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById(eq("SKY"));
    String expectedJson = mapper.writeValueAsString(sky);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_cannot_get_by_id_when_id_does_not_exist() throws Exception {

    when(ucsbOrganizationRepository.findById(eq("FAKE"))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization").param("orgCode", "FAKE"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById(eq("FAKE"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id FAKE not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {

    UCSBOrganization skyOrig =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("Skydiving Club")
            .orgTranslation("UCSB Skydiving Club")
            .inactive(false)
            .build();

    UCSBOrganization skyEdited =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("Sky Club")
            .orgTranslation("UCSB Sky Club")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(skyEdited);

    when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(skyOrig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization")
                    .param("orgCode", "SKY")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("SKY");
    verify(ucsbOrganizationRepository, times(1)).save(skyEdited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {

    UCSBOrganization editedOrg =
        UCSBOrganization.builder()
            .orgCode("FAKE")
            .orgTranslationShort("Fake Club")
            .orgTranslation("UCSB Fake Club")
            .inactive(false)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrg);

    when(ucsbOrganizationRepository.findById(eq("FAKE"))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization")
                    .param("orgCode", "FAKE")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("FAKE");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id FAKE not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_organization() throws Exception {

    UCSBOrganization sky =
        UCSBOrganization.builder()
            .orgCode("SKY")
            .orgTranslationShort("Skydiving Club")
            .orgTranslation("UCSB Skydiving Club")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("SKY"))).thenReturn(Optional.of(sky));

    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization").param("orgCode", "SKY").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("SKY");
    verify(ucsbOrganizationRepository, times(1)).delete(any());
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id SKY deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_nonexistent_organization_and_gets_right_error_message()
      throws Exception {

    when(ucsbOrganizationRepository.findById(eq("FAKE"))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization").param("orgCode", "FAKE").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("FAKE");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id FAKE not found", json.get("message"));
  }
}
