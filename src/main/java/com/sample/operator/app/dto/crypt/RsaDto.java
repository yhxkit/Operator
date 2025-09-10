package com.sample.operator.app.dto.crypt;

import lombok.Builder;
import lombok.Data;

import java.security.PrivateKey;

@Data
@Builder
public class RsaDto {
    public PrivateKey privateKey;
}
