package yapp.buddycon.web.coupon.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import yapp.buddycon.common.response.DefaultResponseDto;
import yapp.buddycon.web.auth.adapter.out.AuthMember;
import yapp.buddycon.web.coupon.adapter.in.response.*;
import yapp.buddycon.web.coupon.adapter.in.request.GifticonCreationRequestDto;
import yapp.buddycon.web.coupon.adapter.in.response.CouponsResponseDto;
import yapp.buddycon.web.coupon.adapter.in.response.CustomCouponInfoResponseDto;
import yapp.buddycon.web.coupon.adapter.in.response.GifticonInfoResponseDto;
import yapp.buddycon.web.coupon.application.port.in.CouponUseCase;
import yapp.buddycon.web.coupon.application.port.out.CouponToAwsS3Port;
import yapp.buddycon.web.coupon.application.port.out.CouponCommandPort;
import yapp.buddycon.web.coupon.application.port.out.CouponQueryPort;
import yapp.buddycon.web.coupon.application.port.out.CouponToMemberQueryPort;
import yapp.buddycon.web.coupon.domain.Coupon;
import yapp.buddycon.web.coupon.application.port.out.CouponToSharedCouponQueryPort;

import java.time.LocalDate;
import java.util.List;
import yapp.buddycon.web.coupon.domain.CouponInfo;
import yapp.buddycon.web.coupon.domain.CouponState;
import yapp.buddycon.web.coupon.domain.CouponType;

@Service
@RequiredArgsConstructor
public class CouponService implements CouponUseCase {

  private final CouponCommandPort couponCommandPort;
  private final CouponQueryPort couponQueryPort;
  private final CouponToSharedCouponQueryPort couponToSharedCouponQueryPort;
  private final CouponToMemberQueryPort couponToMemberQueryPort;
  private final CouponToAwsS3Port couponToAwsS3Port;

  @Override
  public List<CouponsResponseDto> getSortedGifticons(boolean usable, Pageable pageable, AuthMember authMember) {
    Sort sort = SortPageableValidation.validateSortProperty(pageable.getSort().toString());
    PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    if(usable) return couponQueryPort.findUsableGifticonsSortedBy(pageRequest, authMember.id());
    return couponQueryPort.findUsedGifticonsSortedBy(pageRequest, authMember.id());
  }

  @Override
  public List<CouponsResponseDto> getSortedCustomCoupons(boolean usable, Pageable pageable, AuthMember authMember) {
    Sort sort = SortPageableValidation.validateSortProperty(pageable.getSort().toString());
    PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    if(usable) return couponQueryPort.findUsableCustomCouponsSortedBy(pageRequest, authMember.id());
    return couponQueryPort.findUsedCustomCouponsSortedBy(pageRequest, authMember.id());
  }

  @Override
  public GifticonInfoResponseDto getGifticonInfo(Long memberId, Long couponId) {
    return couponQueryPort.findGifticonInfo(memberId, couponId);
  }

  @Override
  public CustomCouponInfoResponseDto getCustomCouponInfo(Long memberId, Long couponId) {
    return couponQueryPort.findCustomCouponInfo(memberId, couponId);
  }

  @Override
  public DefaultResponseDto makeGifticon(GifticonCreationRequestDto gifticonCreationRequestDto,
      MultipartFile image, AuthMember authMember) {
    // empty file
    if (image.isEmpty()) return new DefaultResponseDto(false, "잘못된 이미지입니다.");

    // try to upload image
    String imageUrl = couponToAwsS3Port.upload(image);

    // create coupon
    Coupon coupon = Coupon.create(couponToMemberQueryPort.findById(authMember.id()),
        CouponInfo.valueOf(gifticonCreationRequestDto, imageUrl),
        CouponType.REAL);
    couponCommandPort.createCoupon(coupon);

    return new DefaultResponseDto(true, "기프티콘이 생성되었습니다.");
  }

  @Override
  @Transactional
  public DefaultResponseDto deleteCoupon(Long memberId, Long couponId) {
    Coupon coupon = couponQueryPort.findById(couponId);
    couponCommandPort.deleteCoupon(memberId, coupon.getId());
    return new DefaultResponseDto(true, "쿠폰을 삭제하였습니다.");
  }

  @Override
  public SharedGifticonInfoResponseDto getSharedGifticonInfoFromBarcode(String barcode) {
    return couponToSharedCouponQueryPort.findSharedGifticonByBarcode(barcode);
  }

  @Override
  public SharedCustomCouponResponseDto getSharedCustomCouponInfoFromBarcode(String barcode) {
    return couponToSharedCouponQueryPort.findSharedCustomCouponByBarcode(barcode);
  }

  @Override
  @Transactional
  public DefaultResponseDto changeCouponState(Long memberId, Long couponId, String state) {
    if (state.equals(CouponState.USABLE.name())) {
      Coupon coupon = couponQueryPort.findCouponUsed(memberId, couponId, LocalDate.now());
      couponCommandPort.changeStateUsedToUsable(memberId, coupon.getId());
    } else {
      couponCommandPort.changeStateUsableToUsed(memberId, couponId);
    }
    return new DefaultResponseDto(true, "상태를 변경하였습니다.");
  }
}
