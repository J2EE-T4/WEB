package com.interacthub.controller;

import com.interacthub.dto.common.ApiResponse;
import com.interacthub.dto.profile.*;
import com.interacthub.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController extends BaseController {
    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        return ResponseEntity.ok(profileService.getProfile(getCurrentUserId(), getCurrentUserId()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable String userId) {
        return ResponseEntity.ok(profileService.getProfile(userId, getCurrentUserId()));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(getCurrentUserId(), request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam MultipartFile file) {
        String url = profileService.uploadAvatar(getCurrentUserId(), file);
        return ResponseEntity.ok(Map.of("avatarUrl", url));
    }

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Map<String, Boolean>> toggleFollow(@PathVariable String userId) {
        boolean isFollowing = profileService.toggleFollow(getCurrentUserId(), userId);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<ProfileResponse>> getFollowers(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(profileService.getFollowers(userId, page, size));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<ProfileResponse>> getFollowing(
            @PathVariable String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(profileService.getFollowing(userId, page, size));
    }
}
