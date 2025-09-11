package com.sample.operator.app.dto.crypt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptUploadDto
{
    MultipartFile file1;
    MultipartFile file2;

    String targetData;

    String optionData1;
    String optionData2;
}
