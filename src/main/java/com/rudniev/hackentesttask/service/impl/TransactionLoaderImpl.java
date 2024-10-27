package com.rudniev.hackentesttask.service.impl;

import com.rudniev.hackentesttask.entity.EthereumTransaction;
import com.rudniev.hackentesttask.entity.LoadState;
import com.rudniev.hackentesttask.repository.StateRepository;
import com.rudniev.hackentesttask.service.TransactionLoader;
import com.rudniev.hackentesttask.service.TransactionService;
import com.rudniev.hackentesttask.utils.TransactionMapper;
import io.reactivex.disposables.Disposable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionLoaderImpl implements TransactionLoader {

    private static final Logger log = LoggerFactory.getLogger(TransactionLoaderImpl.class);

    @Value("${ethereum.endpoint}")
    private String ethereumEndpoint;

    @Value("${batch.size:500}")
    private int batchSize;

    private final TransactionService transactionService;
    private final StateRepository stateRepository;
    private Web3j web3j;
    private Disposable subscription;

    private final List<EthereumTransaction> transactionBatch = new ArrayList<>();


    @Override
    @PostConstruct
    public void load() {
        this.web3j = Web3j.build(new HttpService(ethereumEndpoint));

        var startBlock = getLastProcessedBlock();
        subscribeToBlocks(startBlock);
    }

    private long getLastProcessedBlock() {
        var status = stateRepository.findById(1);
        return status.map(state -> state.getLastProcessedBlock() + 1).orElse(0L);
    }

    private void subscribeToBlocks(long startBlock) {


        BigInteger bigIntStartBlock;
        if (startBlock == 0) {
            try {
                bigIntStartBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock().getNumber();
            } catch (IOException e) {
                log.error("Failed to get latest block number, starting from 0", e);
                bigIntStartBlock = BigInteger.valueOf(0L);
            }
        } else {
            bigIntStartBlock = BigInteger.valueOf(startBlock);
        }


        log.info("Subscribing to blocks from block {}", bigIntStartBlock.longValue());

        subscription = web3j
                .replayPastAndFutureBlocksFlowable(DefaultBlockParameter.valueOf(bigIntStartBlock), true)
                .subscribe(this::processBlock, this::handleError);
    }

    private void processBlock(EthBlock ethBlock) {
        try {
            var blockNumber = ethBlock.getBlock().getNumber().longValue();
            log.info("Processing block: {}", blockNumber);

            ethBlock.getBlock().getTransactions().forEach(txObject -> {
                Transaction transaction = (Transaction) txObject.get();
                processTransaction(transaction);
            });

            flushBatch();

            saveLastProcessedBlock(blockNumber);
        } catch (Exception e) {
            log.error("Error processing block: {}", e.getMessage());
        }
    }

    private void flushBatch() {
        if (!transactionBatch.isEmpty()) {
            try {
                transactionService.saveTransactions(transactionBatch);
                log.info("Saved batch of {} transactions", transactionBatch.size());
                transactionBatch.clear();
            } catch (Exception e) {
                log.error("Error saving batch: {}", e.getMessage());
            }
        }
    }

    private void processTransaction(Transaction tx) {
        try {
            if (transactionService.existsById(tx.getHash())) {
                log.info("Skipping duplicate transaction: {}", tx.getHash());
                return;
            }

            var ethereumTransaction = TransactionMapper.INSTANCE.externalTransactionToLocal(tx);
            transactionBatch.add(ethereumTransaction);

            if (transactionBatch.size() >= batchSize) {
                flushBatch();
            }

            log.info("Processed transaction: {}", tx.getHash());
        } catch (Exception e) {
            log.error("Error processing transaction {}: {}", tx.getHash(), e.getMessage());
        }
    }

    private void handleError(Throwable error) {
        log.error("Error in subscription: {}", error.getMessage());

        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }

        var lastProcessedBlock = getLastProcessedBlock();
        subscribeToBlocks(lastProcessedBlock);
    }


    private void saveLastProcessedBlock(long blockNumber) {
        var loadState = new LoadState(1, blockNumber);
        stateRepository.save(loadState);
    }
}
