package com.batm.dto;

import com.batm.entity.Transaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChainalysisResponseDTO {
    private String transferReference;
    private String asset;
    private String clusterName;
    private String clusterCategory;
    private String rating;
    private Transaction transaction;
}
