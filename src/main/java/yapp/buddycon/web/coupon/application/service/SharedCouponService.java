package yapp.buddycon.web.coupon.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import yapp.buddycon.web.coupon.adapter.in.response.SharedCouponsResponseDto;
import yapp.buddycon.web.coupon.application.port.in.SharedCouponUseCase;
import yapp.buddycon.web.coupon.application.port.out.SharedCouponCommandPort;
import yapp.buddycon.web.coupon.application.port.out.SharedCouponQueryPort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SharedCouponService implements SharedCouponUseCase {

  private final SharedCouponCommandPort sharedCouponCommandPort;
  private final SharedCouponQueryPort sharedCouponQueryPort;

  @Override
  public List<SharedCouponsResponseDto> getSortedSharedCoupons(boolean unshared, Pageable pageable, Long memberId) {
    Sort sort = SortPageableValidation.validateSortProperty(pageable.getSort().toString());
    PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    if(unshared) return sharedCouponQueryPort.findUnsharedCustomCouponsSortedBy(memberId, pageRequest);
    else return sharedCouponQueryPort.findCustomCouponsSortedBy(memberId, pageRequest);
  }
}