package uz.pdp.springsecurity.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.pdp.springsecurity.repository.BalanceHistoryRepository;

@Service
@RequiredArgsConstructor
public class BalanceHistoryService {
    private final BalanceHistoryRepository repository;
}
