package com.interacthub.controller;

import com.interacthub.dto.common.ApiResponse;
import com.interacthub.dto.friend.FriendDto;
import com.interacthub.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController extends BaseController {
    private final FriendService friendService;

    @PostMapping("/request/{userId}")
    public ResponseEntity<ApiResponse<String>> sendRequest(@PathVariable String userId) {
        friendService.sendFriendRequest(getCurrentUserId(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Friend request sent", null));
    }

    @PostMapping("/accept/{senderId}")
    public ResponseEntity<ApiResponse<String>> acceptRequest(@PathVariable String senderId) {
        friendService.acceptFriendRequest(getCurrentUserId(), senderId);
        return ResponseEntity.ok(ApiResponse.ok("Friend request accepted", null));
    }

    @PostMapping("/reject/{senderId}")
    public ResponseEntity<ApiResponse<String>> rejectRequest(@PathVariable String senderId) {
        friendService.rejectFriendRequest(getCurrentUserId(), senderId);
        return ResponseEntity.ok(ApiResponse.ok("Friend request rejected", null));
    }

    @PostMapping("/cancel/{userId}")
    public ResponseEntity<ApiResponse<String>> cancelRequest(@PathVariable String userId) {
        friendService.cancelFriendRequest(getCurrentUserId(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Friend request cancelled", null));
    }

    @PostMapping("/unfriend/{userId}")
    public ResponseEntity<ApiResponse<String>> unfriend(@PathVariable String userId) {
        friendService.unfriend(getCurrentUserId(), userId);
        return ResponseEntity.ok(ApiResponse.ok("Unfriended successfully", null));
    }

    @GetMapping("/list")
    public ResponseEntity<List<FriendDto>> getFriends() {
        return ResponseEntity.ok(friendService.getFriends(getCurrentUserId()));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendDto>> getRequests() {
        return ResponseEntity.ok(friendService.getPendingRequests(getCurrentUserId()));
    }
}
