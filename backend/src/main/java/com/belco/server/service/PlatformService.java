package com.belco.server.service;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Setter
@Getter
@Service
public class PlatformService {

    private final MongoTemplate mongo;

    private BigDecimal platformSwapFee;
    private Map<String, Long> initialGasLimits;

    public PlatformService(MongoTemplate mongo) {
        this.mongo = mongo;

        Document doc = mongo.getCollection("platform_details").find().first();

        this.platformSwapFee = doc.get("platformSwapFee", Decimal128.class).bigDecimalValue();
        this.initialGasLimits = doc.get("initialGasLimits", Map.class);
    }
}