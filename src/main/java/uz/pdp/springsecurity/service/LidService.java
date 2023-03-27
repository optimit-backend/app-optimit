package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.payload.ApiResponse;
import uz.pdp.springsecurity.payload.LidDto;
import uz.pdp.springsecurity.repository.LidRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LidService {
    private final LidRepository repository;

    public ApiResponse getAll(UUID businessId) {
        return null;
    }

    public ApiResponse getById(UUID id) {
        return null;
    }

    public ApiResponse create(LidDto lidDto) {
        return null;
    }

    public ApiResponse editStatus(UUID id, UUID statusId) {
        return null;
    }

    public ApiResponse delete(UUID id) {
        return null;
    }
}
