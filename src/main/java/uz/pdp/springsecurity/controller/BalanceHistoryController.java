package uz.pdp.springsecurity.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.springsecurity.service.BalanceHistoryService;

@RestController
@RequiredArgsConstructor
public class BalanceHistoryController {
    private final BalanceHistoryService service;
}
