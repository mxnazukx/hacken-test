package com.rudniev.hackentesttask.utils;

import com.rudniev.hackentesttask.entity.EthereumTransaction;
import com.rudniev.hackentesttask.model.ElasticSearchTransactionModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.web3j.protocol.core.methods.response.Transaction;

@Mapper
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    EthereumTransaction externalTransactionToLocal(Transaction transaction);


    @Mapping(target = "v", expression = "java(String.valueOf(transaction.getV()))")
    ElasticSearchTransactionModel entityToESModel(EthereumTransaction transaction);
}
