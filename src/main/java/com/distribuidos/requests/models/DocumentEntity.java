package com.distribuidos.requests.models;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentEntity {

    Long documentId;
    String documentName;
    String description;
    Boolean isVerified;
    String downloadLink;
    Long userDocumentId;
    LocalDateTime creationDate;

}
