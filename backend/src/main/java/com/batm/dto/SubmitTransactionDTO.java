package com.batm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.sf.json.JSONObject;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTransactionDTO {

    private Integer type;

    private String phone;
    private Boolean exists;
    private String message;
    private String image;

    private String hex;

    private JSONObject trx;
}