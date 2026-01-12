package com.ajouchong.service;

import com.ajouchong.common.ApiResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
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
        
        // 파일 크기 설정 (필수) - 정확한 ContentLength 설정
        long fileSize = multipartFile.getSize();
        java.io.InputStream inputStream;
        
        if (fileSize <= 0) {
            // getSize()가 -1을 반환하는 경우, 바이트 배열로 변환하여 정확한 크기 계산
            byte[] fileBytes = multipartFile.getBytes();
            fileSize = fileBytes.length;
            inputStream = new java.io.ByteArrayInputStream(fileBytes);
        } else {
            // 정상적인 크기인 경우
            inputStream = multipartFile.getInputStream();
        }
        
        // ContentLength 메타데이터 설정 (필수)
        metadata.setContentLength(fileSize);
        
        // ContentType 설정
        String contentType = multipartFile.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream"; // 기본값
        }
        metadata.setContentType(contentType);

        // PutObjectRequest를 사용하여 메타데이터와 함께 업로드
        PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucket, 
                uniqueFilename, 
                inputStream, 
                metadata
        );
        amazonS3.putObject(putObjectRequest);

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