package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeListDTO {

    private Integer buyTotal;
    private Integer sellTotal;
    private Integer myTotal;

    private List<TradeDetailsDTO> buyTrades;
    private List<TradeDetailsDTO> sellTrades;
    private List<TradeDetailsDTO> myTrades;
}