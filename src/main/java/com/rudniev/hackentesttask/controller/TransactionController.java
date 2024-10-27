package com.rudniev.hackentesttask.controller;

import com.rudniev.hackentesttask.entity.EthereumTransaction;
import com.rudniev.hackentesttask.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/search")
    public Page<EthereumTransaction> search(@RequestParam String query, Pageable pageable) {
        return transactionService.searchByFullText(query, pageable);
    }
}
