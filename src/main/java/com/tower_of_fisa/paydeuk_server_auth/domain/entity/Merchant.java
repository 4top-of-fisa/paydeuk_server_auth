package com.tower_of_fisa.paydeuk_server_auth.domain.entity;

import com.tower_of_fisa.paydeuk_server_auth.domain.enums.MerchantCategory;
import com.tower_of_fisa.paydeuk_server_auth.global.common.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "merchant")
public class Merchant extends BaseEntity {
  @Id
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "name", length = 20, nullable = false)
  private String name;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;

  @Column(name = "commission_rate", length = 10, nullable = false)
  private String commissionRate;

  @Column(name = "business_number", length = 20, nullable = false)
  private String businessNumber;

  @Column(name = "is_deleted", nullable = false, length = 20)
  private boolean isDeleted;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false)
  private MerchantCategory category;

  @Column(name = "manager_name", length = 20, nullable = false)
  private String managerName;

  @Column(name = "phone", length = 20, nullable = false)
  private String phone;

  @Column(name = "manager_phone", length = 20, nullable = false)
  private String managerPhone;

  @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Benefit> benefits = new ArrayList<>();

  @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Payment> payments = new ArrayList<>();

  public void changeIsActive(boolean value) {
    this.isActive = value;
  }

  public void changeIsDeleted(boolean value) {
    this.isDeleted = value;
  }
}
