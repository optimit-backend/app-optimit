package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LessonDto;
import uz.pdp.springsecurity.repository.LessonRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {
    private final LessonRepository lessonRepository;

    public ApiResponse add(LessonDto lessonDto) {
        return new ApiResponse("SUCCESS",  true);
    }

    public ApiResponse edit(UUID lessonId, LessonDto lessonDto) {
        return new ApiResponse("SUCCESS",  true);
    }

    public ApiResponse getAll() {
        return new ApiResponse("SUCCESS",  true);
    }

    public ApiResponse getOne(UUID lessonId) {
        return new ApiResponse("SUCCESS",  true);
    }

    public ApiResponse delete(UUID lessonId) {
        return new ApiResponse("SUCCESS",  true);
    }
}
