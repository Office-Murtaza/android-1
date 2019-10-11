package com.batm.dto;

import com.batm.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChainalysisResponseDTO {

    private String transferReference;
    private String asset;
    private String clusterName;
    private String clusterCategory;
    private String rating;
    private Transaction transaction;
}