package com.printims.module.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.printims.module.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_customer")
public class Customer extends BaseEntity {

    private String customerNo;
    private String customerName;
    private String contactPerson;
    private String phone;
    private String address;
    private String email;
    private String customerLevel;
    private String remark;
}
