package yapp.buddycon.web.coupon.adapter.in;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yapp.buddycon.common.response.DefaultIdResponseDto;
import yapp.buddycon.common.response.DefaultResponseDto;
import yapp.buddycon.web.auth.adapter.out.AuthMember;
import yapp.buddycon.web.coupon.adapter.in.request.SharedCouponForCustomCouponCreationRequestDto;
import yapp.buddycon.web.coupon.adapter.in.request.SharedCouponForGifticonCreationRequestDto;
import yapp.buddycon.web.coupon.adapter.in.response.SharedCouponsResponseDto;
import yapp.buddycon.web.coupon.application.port.in.SharedCouponUseCase;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/coupon/share")
public class SharedCouponController {

  private final SharedCouponUseCase sharedCouponUseCase;

  @GetMapping("")
  @Operation(summary = "만든쿠폰 정렬 조회")
  public List<SharedCouponsResponseDto> getSortedSharedCoupons(@RequestParam("unshared") boolean unshared, Pageable pageable, AuthMember authMember) {
    return sharedCouponUseCase.getSortedSharedCoupons(unshared, pageable, authMember.id());
  }

  @PatchMapping("/{id}/state")
  @Operation(summary = "제작티콘 선물 후 공유완료 상태로 변경", description = "카카오톡으로 메시지를 보내기를 성공한 경우 api")
  public DefaultResponseDto changeCustomCouponState(@PathVariable("id") long id, AuthMember authMember) {
    return sharedCouponUseCase.changeCustomCouponState(id, authMember.id());
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "만든쿠폰 삭제")
  public DefaultResponseDto deleteSharedCoupon(@PathVariable("id") long id, AuthMember authMember) {
    return sharedCouponUseCase.deleteSharedCoupon(authMember.id(), id);
  }

  @PostMapping(value = "/gifticon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "기프티콘으로 만든 쿠폰 추가 (재활용)")
  public DefaultIdResponseDto makeSharedCouponForGifticon(@RequestBody SharedCouponForGifticonCreationRequestDto dto,
      @RequestPart MultipartFile image, AuthMember authMember) {
    return sharedCouponUseCase.makeSharedCouponForGifticon(dto, image, authMember.id());
  }

  @PostMapping(value = "/custom-coupon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "제작티콘으로 만든 쿠폰 추가")
  public DefaultIdResponseDto makeSharedCouponForCustomCoupon(@RequestBody SharedCouponForCustomCouponCreationRequestDto dto,
      @RequestPart MultipartFile image, AuthMember authMember) {
    return sharedCouponUseCase.makeSharedCouponForCustomCoupon(dto, image, authMember.id());
  }

}
