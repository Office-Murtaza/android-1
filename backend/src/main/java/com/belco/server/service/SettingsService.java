package com.belco.server.service;

import lombok.Getter;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Service
public class SettingsService {

    private final MongoTemplate mongo;

    private BigDecimal platformSwapFee;
    private BigDecimal platformTradeFee;
    private Map<String, Long> initialGasLimits;

    public SettingsService(MongoTemplate mongo) {
        this.mongo = mongo;

        Document doc = mongo.getCollection("settings").find().first();

        this.platformSwapFee = doc.get("platformSwapFee", Decimal128.class).bigDecimalValue();
        this.platformTradeFee = doc.get("platformTradeFee", Decimal128.class).bigDecimalValue();
        this.initialGasLimits = doc.get("initialGasLimits", Map.class);

        int i = 0;
    }
}