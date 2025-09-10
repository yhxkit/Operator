package com.sample.operator.app.dto.crypt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AesDto
{
    String aes256Iv;
    String aes256Key;
}
