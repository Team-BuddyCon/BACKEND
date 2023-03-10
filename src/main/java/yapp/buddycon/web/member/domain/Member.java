package yapp.buddycon.web.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import yapp.buddycon.common.domain.BaseEntity;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Member extends BaseEntity {

  @Column(name = "client_id", unique = true)
  private Long clientId;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "name")
  private String name;

  @Column(name = "gender")
  private String gender;

  @Column(name = "age_range")
  private String ageRange;

  @Column(name = "deleted")
  private boolean deleted;

  public static Member create(Long clientId, String email, String name, String gender, String ageRange) {
    return Member.builder()
      .clientId(clientId)
      .email(email)
      .name(name)
      .gender(gender)
      .ageRange(ageRange)
      .deleted(false)
      .build();
  }

  public void delete() {
    this.deleted = true;
  }

}
