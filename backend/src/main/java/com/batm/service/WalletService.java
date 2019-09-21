package com.batm.service;

import com.batm.util.AES;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wallet.core.jni.HDWallet;

import javax.annotation.PostConstruct;

@Service
@Slf4j
@Getter
public class WalletService {

//    static {
//        System.loadLibrary("TrustWalletCore");
//    }

    @Value("${wallet.seed}")
    private String walletSeed;

    @Value("${wallet.seed.key}")
    private String walletSeedKey;

    private HDWallet wallet = null;

    @PostConstruct
    public void init() {
        //wallet = new HDWallet(128, AES.decrypt(walletSeed, walletSeedKey));
    }
}