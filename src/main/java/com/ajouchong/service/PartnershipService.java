package com.ajouchong.service;

import com.ajouchong.dto.request.PartnershipRequestDto;
import com.ajouchong.dto.response.PartnershipResponseDto;
import com.ajouchong.entity.*;
import com.ajouchong.jwt.JwtTokenProvider;
import com.ajouchong.repository.MemberRepository;
import com.ajouchong.repository.PartnershipLikeRepository;
import com.ajouchong.repository.PartnershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnershipService {
    private final PartnershipRepository partnershipRepository;
    private final PartnershipLikeRepository partnershipLikeRepository;
    private final S3UploadService s3UploadService;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public PartnershipResponseDto savePartnership(PartnershipRequestDto requestDto) throws IOException {
        Partnership partnership = new Partnership();

        partnership.setPsTitle(requestDto.getTitle());
        partnership.setPsContent(requestDto.getContent());
        partnership.setPsCreateTime(LocalDateTime.now());
        partnership.setPsUpdateTime(LocalDateTime.now());

        List<String> imageUrls = new ArrayList<>();
        if (requestDto.getImageFiles() != null && !requestDto.getImageFiles().isEmpty()) {
            for (MultipartFile file : requestDto.getImageFiles()) {
                String image = s3UploadService.saveFile(file); // S3에 업로드 후 URL 반환
                imageUrls.add(image);
            }
        }

        partnership.setImageUrls(imageUrls);
        Partnership savedPartnership = partnershipRepository.save(partnership);

        return convertToPsDto(savedPartnership, null);
    }

//    @Transactional
//    public PartnershipResponseDto changePartnership(Long id, PartnershipRequestDto requestDto) {
//        Partnership partnership = partnershipRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException(id + "번 게시글을 찾을 수 없습니다."));
//
//        partnership.setPsTitle(requestDto.getTitle());
//        partnership.setPsContent(requestDto.getContent());
//        partnership.setPsUpdateTime(LocalDateTime.now());
//
//        List<PartnershipImage> existingImages = partnership.getImages();
//
//        existingImages.clear();
//
//        for (int i = 0; i < requestDto.getImageUrls().size(); i++) {
//            String imageUrl = requestDto.getImageUrls().get(i);
//            PartnershipImage newImage = new PartnershipImage();
//            newImage.setImageUrl(imageUrl);
//            newImage.setImageOrder(i);
//            newImage.setPartnership(partnership);
//            existingImages.add(newImage);
//        }
//
//        partnershipRepository.save(partnership);
//        return convertToDto(partnership);
//    }

    public List<PartnershipResponseDto> getLatestPartnerships() {
        List<Partnership> partnerships = partnershipRepository.findAll(Sort.by(Sort.Direction.DESC, "psCreateTime"));

        return partnerships.stream()
                .map(partnership -> new PartnershipResponseDto(partnership, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PartnershipResponseDto getPartnershipById(Long id, String token) {
        Partnership partnership = partnershipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(id + "번 게시글을 찾을 수 없습니다."));

        partnership.setPsHitCnt(partnership.getPsHitCnt() + 1);
        String email = (token != null) ? jwtTokenProvider.getEmailFromToken(token) : null;

        return convertToPsDto(partnership, email);
    }

    @Transactional
    public void deletePartnership(Long id) {
        partnershipRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> togglePsLike(Long id, String token) {
        String email = jwtTokenProvider.getEmailFromToken(token);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<PartnershipLke> exLike = partnershipLikeRepository.findByMemberAndPsPostId(member, id);
        boolean isLiked;

        if (exLike.isPresent()) {
            partnershipLikeRepository.delete(exLike.get());
            isLiked = false;
        } else {
            PartnershipLke partnershipLike = new PartnershipLke();
            partnershipLike.setMember(member);
            partnershipLike.setPsPostId(id);
            partnershipLikeRepository.save(partnershipLike);
            isLiked = true;
        }

        long likeCount = partnershipLikeRepository.countByPsPostId(id);
        Partnership partnership = partnershipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(id + "번 게시글을 찾을 수 없습니다."));
        partnership.setPsUserLikeCnt((int) likeCount);
        partnershipRepository.save(partnership);

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLiked);
        result.put("likeCount", likeCount);
        return result;
    }

    @Transactional
    public boolean isUserLikedPartnership(Long psPostId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Optional<PartnershipLke> existingLike = partnershipLikeRepository.findByMemberAndPsPostId(member, psPostId);

        return existingLike.isPresent();
    }

    @Transactional
    public PartnershipResponseDto convertToPsDto(Partnership partnership, String email) {
        boolean likedByCurrentUser = false;
        if (email != null) {
            likedByCurrentUser = isUserLikedPartnership(partnership.getPsPostId(), email);
        }

        return new PartnershipResponseDto(partnership, likedByCurrentUser);
    }
}
