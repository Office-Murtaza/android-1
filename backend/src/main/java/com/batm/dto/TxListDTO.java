package com.batm.dto;

import com.batm.entity.TransactionRecord;
import com.batm.entity.TransactionRecordWallet;
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

    List<TransactionRecord> buySellList = new ArrayList<>();
    List<TransactionRecordWallet> giftList = new ArrayList<>();
    List<TransactionRecordWallet> exchangeList = new ArrayList<>();
}