package com.rudniev.hackentesttask.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.rudniev.hackentesttask.entity.EthereumTransaction;
import com.rudniev.hackentesttask.model.ElasticSearchTransactionModel;
import com.rudniev.hackentesttask.repository.TransactionRepository;
import com.rudniev.hackentesttask.service.TransactionService;
import com.rudniev.hackentesttask.utils.TransactionMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionLoaderImpl.class);
    private final TransactionRepository transactionRepository;
    private final ElasticsearchClient elasticsearchClient;


    @Override
    @Transactional
    public void saveTransactions(List<EthereumTransaction> transactions) {
        List<EthereumTransaction> savedTransactions = transactionRepository.saveAll(transactions);

        try {
            BulkRequest.Builder bulkRequest = new BulkRequest.Builder();

            for (EthereumTransaction transaction : savedTransactions) {
                ElasticSearchTransactionModel esTransaction = TransactionMapper.INSTANCE.entityToESModel(transaction);
                bulkRequest.operations(op -> op
                        .index(idx -> idx
                                .index("ethereum_transaction")
                                .id(esTransaction.getHash())
                                .document(esTransaction)
                        )
                );
            }

            BulkResponse response = elasticsearchClient.bulk(bulkRequest.build());

            if (response.errors()) {
                log.error("Transactions were not saved to elastic search");
            }

        } catch (IOException e) {
            log.error("Transactions were not saved to elastic search");
        }
    }

    @Override
    public Page<EthereumTransaction> searchByFullText(String text, Pageable pageable) {
        List<String> fields = Arrays.stream(EthereumTransaction.class.getDeclaredFields())
                .map(Field::getName)
                .toList();


        try {
            Query query = new Query.Builder()
                    .multiMatch(QueryBuilders.multiMatch()
                            .query(text)
                            .fields("*")
                            .build())
                    .build();

            SearchRequest request = SearchRequest.of(b -> b
                    .index("ethereum_transaction")
                    .query(query)
                    .from((int) pageable.getOffset())
                    .size(pageable.getPageSize())
            );

            SearchResponse<EthereumTransaction> response = elasticsearchClient.search(request, EthereumTransaction.class);

            List<EthereumTransaction> transactions = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            return new PageImpl<>(transactions, pageable, response.hits().total().value());
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute search query", e);
        }
    }

    @Override
    public boolean existsById(String hash) {
        return transactionRepository.existsById(hash);
    }
}
