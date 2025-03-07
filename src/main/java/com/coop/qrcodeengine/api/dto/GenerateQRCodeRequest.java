package com.coop.qrcodeengine.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQRCodeRequest {
    // TODO: Make this use a hashmap to make it extensible
    String merchantName;
    String merchantCity;
    String postalCode;
    String merchantAccountInformation;
    String merchantCategoryCode;
}
