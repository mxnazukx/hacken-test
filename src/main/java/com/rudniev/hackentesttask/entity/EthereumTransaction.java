package com.rudniev.hackentesttask.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity(name = "ethereum_transaction")
public class EthereumTransaction {
    @Id
    private String hash;
    private String nonce;
    private String blockHash;
    private String blockNumber;
    private String transactionIndex;

    @Column(name = "\"from\"")
    private String from;

    @Column(name = "\"to\"")
    private String to;
    private String value;
    private String gasPrice;
    private String gas;

    @Column(columnDefinition = "TEXT")
    private String input;
    private String creates;
    private String publicKey;
    private String raw;
    private String r;
    private String s;
    private long v;
}
