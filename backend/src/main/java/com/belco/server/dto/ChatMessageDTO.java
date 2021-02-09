package com.belco.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document
public class ChatMessageDTO {

    @Id
    private Long id;
    private Long orderId;
    private Long senderId;
    private Long recipientId;
    private String message;
    private long timestamp;
}