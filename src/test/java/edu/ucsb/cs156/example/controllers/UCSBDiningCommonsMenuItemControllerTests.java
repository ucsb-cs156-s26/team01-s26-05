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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
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

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockitoBean UserRepository userRepository;

  // Tests for GET /api/UCSBDiningCommonsMenuItem/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBDiningCommonsMenuItem/all")).andExpect(status().is(200));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menu_items() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem baked_pesto_pasta_with_chicken =
        UCSBDiningCommonsMenuItem.builder()
            .code("BPPC-ORTEGA")
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem tofu_banh_mi_sandwich =
        UCSBDiningCommonsMenuItem.builder()
            .code("TBMS-ORTEGA")
            .diningCommonsCode("ortega")
            .name("Tofu Banh Mi Sandwich (v)")
            .station("Entree Specials")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>();
    expectedItems.addAll(Arrays.asList(baked_pesto_pasta_with_chicken, tofu_banh_mi_sandwich));

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedItems);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for GET /api/UCSBDiningCommonsMenuItem?code=...

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItem").param("code", "BPPC-ORTEGA"))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange

    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .code("BPPC-ORTEGA")
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq("BPPC-ORTEGA")))
        .thenReturn(Optional.of(item));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("code", "BPPC-ORTEGA"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq("BPPC-ORTEGA"));
    String expectedJson = mapper.writeValueAsString(item);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbDiningCommonsMenuItemRepository.findById(eq("nope-ortega")))
        .thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("code", "nope-ortega"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq("nope-ortega"));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBDiningCommonsMenuItem with id nope-ortega not found", json.get("message"));
  }

  // Tests for POST /api/UCSBDiningCommonsMenuItem/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("code", "BPPC-ORTEGA")
                .param("diningCommonsCode", "ortega")
                .param("name", "Baked Pesto Pasta with Chicken")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("code", "BPPC-ORTEGA")
                .param("diningCommonsCode", "ortega")
                .param("name", "Baked Pesto Pasta with Chicken")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .code("BPPC-ORTEGA")
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(eq(item))).thenReturn(item);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBDiningCommonsMenuItem/post")
                    .param("code", "BPPC-ORTEGA")
                    .param("diningCommonsCode", "ortega")
                    .param("name", "Baked Pesto Pasta with Chicken")
                    .param("station", "Entree Specials")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(item);
    String expectedJson = mapper.writeValueAsString(item);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for PUT /api/UCSBDiningCommonsMenuItem?code=...

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem original =
        UCSBDiningCommonsMenuItem.builder()
            .code("BPPC-ORTEGA")
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem edited =
        UCSBDiningCommonsMenuItem.builder()
            .code("BPPC-ORTEGA")
            .diningCommonsCode("portola")
            .name("Baked Pesto Pasta")
            .station("Greens & Grains")
            .build();

    String requestBody = mapper.writeValueAsString(edited);

    when(ucsbDiningCommonsMenuItemRepository.findById(eq("BPPC-ORTEGA")))
        .thenReturn(Optional.of(original));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("code", "BPPC-ORTEGA")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById("BPPC-ORTEGA");
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(edited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_menu_item_that_does_not_exist() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem editedItem =
        UCSBDiningCommonsMenuItem.builder()
            .code("nope-ortega")
            .diningCommonsCode("ortega")
            .name("Imaginary Item")
            .station("Nowhere")
            .build();

    String requestBody = mapper.writeValueAsString(editedItem);

    when(ucsbDiningCommonsMenuItemRepository.findById(eq("nope-ortega")))
        .thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("code", "nope-ortega")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById("nope-ortega");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id nope-ortega not found", json.get("message"));
  }

  // Tests for DELETE /api/UCSBDiningCommonsMenuItem?code=...

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem item =
        UCSBDiningCommonsMenuItem.builder()
            .code("BPPC-ORTEGA")
            .diningCommonsCode("ortega")
            .name("Baked Pesto Pasta with Chicken")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq("BPPC-ORTEGA")))
        .thenReturn(Optional.of(item));

    // act
    MvcResult response =
        mockMvc
            .perform(
                delete("/api/UCSBDiningCommonsMenuItem").param("code", "BPPC-ORTEGA").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById("BPPC-ORTEGA");
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id BPPC-ORTEGA deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_menu_item_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(ucsbDiningCommonsMenuItemRepository.findById(eq("nope-ortega")))
        .thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                delete("/api/UCSBDiningCommonsMenuItem").param("code", "nope-ortega").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById("nope-ortega");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id nope-ortega not found", json.get("message"));
  }
}
