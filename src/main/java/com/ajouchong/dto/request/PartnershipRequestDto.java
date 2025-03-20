package com.ajouchong.dto.request;

import com.ajouchong.entity.Partnership;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@Builder
public class PartnershipRequestDto {
    private String title;
    private String content;
    private List<MultipartFile> imageFiles = new ArrayList<>();

    public Partnership createPartnerPost(List<String> imageUrls) {
        Partnership partnership = new Partnership();
        partnership.setPsTitle(this.title);
        partnership.setPsContent(this.content);
        partnership.setImageUrls(imageUrls);

        return partnership;
    }
}
