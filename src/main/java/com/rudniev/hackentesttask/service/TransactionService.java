package com.rudniev.hackentesttask.service;

import com.rudniev.hackentesttask.entity.EthereumTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    void saveTransactions(List<EthereumTransaction> transactions);

    Page<EthereumTransaction> searchByFullText(String text, Pageable pageRequest);

    boolean existsById(String hash);
}

