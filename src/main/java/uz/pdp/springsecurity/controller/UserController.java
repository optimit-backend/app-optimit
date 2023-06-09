package uz.pdp.springsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.springsecurity.annotations.CheckPermission;
import uz.pdp.springsecurity.annotations.CurrentUser;
import uz.pdp.springsecurity.entity.User;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.ProfileDto;
import uz.pdp.springsecurity.payload.UserDto;
import uz.pdp.springsecurity.service.UserService;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;

    /**
     * YANGI USER QOSHISH
     *
     * @param userDto
     * @return ApiResponse(success - > true, message - > ADDED)
     */
    @CheckPermission("ADD_USER")
    @PostMapping()
    public HttpEntity<?> add(@Valid @RequestBody UserDto userDto) {
        ApiResponse apiResponse = userService.add(userDto, false);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * USERNI TAXRIRLASH
     *
     * @param id
     * @param userDto
     * @return ApiResponse(success - > true, message - > EDITED)
     */
    @CheckPermission("EDIT_USER")
    @PutMapping("/{id}")
    public HttpEntity<?> editUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        ApiResponse apiResponse = userService.edit(id, userDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID OQALI BITTA USERNI OLIB CHIQISH
     *
     * @param id
     * @return ApiResponse(success - > true object - > value)
     */
    @CheckPermission("VIEW_USER")
    @GetMapping("/{id}")
    public HttpEntity<?> get(@PathVariable UUID id) {
        ApiResponse apiResponse = userService.get(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * ID ORQALI BITTA USERNI DELETE QILISH
     *
     * @param id
     * @return ApiResponse(success - > true, message - > DELETED)
     */
    @CheckPermission("DELETE_USER")
    @DeleteMapping("/{id}")
    public HttpEntity<?> deleteById(@PathVariable UUID id) {
        ApiResponse apiResponse = userService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 404).body(apiResponse);
    }

    /**
     * OZINI PROFILINI TAXRIRLASH
     *
     * @return ApiResponse(success - > true, message - > UPDATED)
     * @CurrentUser user
     * @RequesBody profileDto
     */
    @CheckPermission("EDIT_MY_PROFILE")
    @PutMapping()
    public ResponseEntity<?> editMyProfile(@CurrentUser User user, @Valid @RequestBody ProfileDto profileDto) {
        ApiResponse apiResponse = userService.editMyProfile(user, profileDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? HttpStatus.OK : HttpStatus.CONFLICT).body(apiResponse);
    }

    /**
     * ROLE_ID ORQALI USERNI OLIB CHIQISH
     *
     * @return ApiResponse(success - > true, message - > FOUND)
     * @Id role_id
     */

    @CheckPermission("VIEW_USER")
    @GetMapping("/get-by-role/{role_id}")
    public HttpEntity<?> getByRole(@PathVariable UUID role_id) {
        ApiResponse apiResponse = userService.getByRole(role_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * BUSINESS_ID ORQALI USERLARNI OLIB CHIQISH
     *
     * @return ApiResponse(success - > true, message - > FOUND)
     * @Id business_id
     */
    @CheckPermission("VIEW_USER_ADMIN")
    @GetMapping("/get-by-business/{business_id}")
    public HttpEntity<?> getAllByBusinessId(@PathVariable UUID business_id) {
        ApiResponse apiResponse = userService.getAllByBusinessId(business_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    /**
     * BRANCH_iD ORQALI USERLARNI OLIB CHIQISH
     *
     * @param branch_id
     * @return ApiResponse(success - > true, message - > FOUND)
     */
    @CheckPermission("VIEW_USER")
    @GetMapping("/get-by-branchId/{branch_id}")
    public HttpEntity<?> getAllByBranchId(@PathVariable UUID branch_id) {
        ApiResponse apiResponse = userService.getAllByBranchId(branch_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_USER")
    @GetMapping("/get-user-by-name/{branch_id}")
    public HttpEntity<?> getAllByUserName(@PathVariable UUID branch_id,
                                          @RequestParam String name,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        ApiResponse apiResponse = userService.getAllByName(branch_id, name, page, size);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_USER")
    @GetMapping("/get-by-patron/{user_id}")
    public HttpEntity<?> getByPatron(@PathVariable UUID user_id) {
        ApiResponse apiResponse = userService.getByPatron(user_id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_USER")
    @GetMapping("/search-by-username/{username}")
    public HttpEntity<?> search(@PathVariable String username) {
        ApiResponse apiResponse = userService.search(username);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

    @CheckPermission("VIEW_USER")
    @PutMapping("/edit-password/{id}")
    public HttpEntity<?> editPassword(@PathVariable UUID id,
                                      @RequestBody String password) {
        ApiResponse apiResponse = userService.editPassword(id,password);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 409).body(apiResponse);
    }

}
