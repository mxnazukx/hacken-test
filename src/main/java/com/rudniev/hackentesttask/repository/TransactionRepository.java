package com.rudniev.hackentesttask.repository;

import com.rudniev.hackentesttask.entity.EthereumTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<EthereumTransaction, String> {
}
