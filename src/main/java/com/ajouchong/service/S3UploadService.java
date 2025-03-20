package com.ajouchong.service;

import com.ajouchong.common.ApiResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String saveFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        amazonS3.putObject(bucket, uniqueFilename, multipartFile.getInputStream(), metadata);
        return amazonS3.getUrl(bucket, uniqueFilename).toString();
    }

    public ApiResponse<UrlResource> downloadImage(String originalFilename) {
        try {
            UrlResource urlResource = new UrlResource(amazonS3.getUrl(bucket, originalFilename));

            // Content-Disposition 헤더 생성
            String contentDisposition = "attachment; filename=\"" + originalFilename + "\"";
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

            return new ApiResponse<>(1, "이미지 다운로드 성공", urlResource);

        } catch (Exception e) {
            return new ApiResponse<>(0, "Failed to download : " + e.getMessage(), null);
        }
    }

    public void deleteImage(String originalFilename)  {
        amazonS3.deleteObject(bucket, originalFilename);
    }

}