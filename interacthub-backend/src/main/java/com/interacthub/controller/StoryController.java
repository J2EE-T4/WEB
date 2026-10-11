package com.interacthub.controller;

import com.interacthub.dto.story.StoryResponse;
import com.interacthub.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/story")
@RequiredArgsConstructor
public class StoryController extends BaseController {
    private final StoryService storyService;

    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> getGlobal(@RequestParam(defaultValue = "20") int take) {
        List<StoryResponse> stories = storyService.getGlobalStories(take);
        return ResponseEntity.ok(Map.of("data", stories, "hasMore", stories.size() == take));
    }

    @GetMapping("/local")
    public ResponseEntity<List<StoryResponse>> getLocal(@RequestParam(defaultValue = "20") int take) {
        return ResponseEntity.ok(storyService.getLocalStories(getCurrentUserId(), take));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoryResponse> create(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String caption) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(storyService.createStory(getCurrentUserId(), file, caption));
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<Void> delete(@PathVariable String storyId) {
        storyService.deleteStory(storyId, getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
