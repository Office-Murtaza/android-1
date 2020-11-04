package com.belco.server.dto;

import com.belco.server.entity.TransactionRecord;
import com.belco.server.entity.TransactionRecordWallet;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TxListDTO {

    List<TransactionRecord> transactionRecords = new ArrayList<>();
    List<TransactionRecordWallet> transactionRecordWallets = new ArrayList<>();
}