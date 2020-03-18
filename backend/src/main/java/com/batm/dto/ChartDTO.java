package com.batm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartDTO {
    private ChartItemDTO day;
    private ChartItemDTO week;
    private ChartItemDTO month;
    private ChartItemDTO threeMonths;
    private ChartItemDTO year;
}
