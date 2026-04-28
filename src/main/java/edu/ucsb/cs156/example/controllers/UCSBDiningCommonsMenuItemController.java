package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDiningCommonsMenuItem */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/UCSBDiningCommonsMenuItem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * This method returns a list of all UCSBDiningCommonsMenuItems.
   *
   * @return a list of all UCSBDiningCommonsMenuItems
   */
  @Operation(summary = "List all UCSB dining commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> items = ucsbDiningCommonsMenuItemRepository.findAll();
    return items;
  }

  /**
   * This method creates a new UCSBDiningCommonsMenuItem. Accessible only to users with the role
   * "ROLE_ADMIN".
   *
   * @param code unique code identifying the menu item
   * @param diningCommonsCode code for the dining commons that serves the item
   * @param name name of the menu item
   * @param station the station within the dining commons where the item is served
   * @return the saved UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Create a new UCSB dining commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postMenuItem(
      @Parameter(name = "code") @RequestParam String code,
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station) {

    UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();
    item.setCode(code);
    item.setDiningCommonsCode(diningCommonsCode);
    item.setName(name);
    item.setStation(station);

    UCSBDiningCommonsMenuItem savedItem = ucsbDiningCommonsMenuItemRepository.save(item);

    return savedItem;
  }
}
