package com.zeroone.star.oauth2.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 医务人员主数据
 * </p>
 *
 * @author tsfmn
 * @since 2026-09-07
 */
@Getter
@Setter
@ToString
@TableName("adm_practitioner")
public class AdmPractitioner implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private String id;

    /**
     * 租户标识
     */
    private String tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 最后更新人
     */
    private String updatedBy;

    /**
     * 软删标记
     */
    private Boolean isDeleted;

    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;

    /**
     * 删除人
     */
    private String deletedBy;

    /**
     * 乐观锁版本号
     */
    private Long version;

    /**
     * 人员姓名
     */
    private String name;

    /**
     * 其他名称 JSON（曾用名、英文名等）
     */
    private String nameJson;

    /**
     * 姓名拼音检索码
     */
    private String pyStr;

    /**
     * 姓名五笔检索码
     */
    private String wbStr;

    /**
     * 行政性别编码
     */
    private String genderEnum;

    /**
     * 出生日期
     */
    private LocalDate birthDate;

    /**
     * 死亡时间
     */
    private LocalDateTime deceasedDate;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 地址-省
     */
    private String addressProvince;

    /**
     * 地址-市
     */
    private String addressCity;

    /**
     * 地址-区/县
     */
    private String addressDistrict;

    /**
     * 地址-街道/详细地址
     */
    private String addressStreet;

    /**
     * 地址扩展 JSON
     */
    private String addressJson;

    /**
     * 人员状态（ACTIVE / INACTIVE / DISCARDED）
     */
    private String statusEnum;

    /**
     * 是否启用（status_enum == ACTIVE 时为 true）
     */
    private Boolean activeFlag;

    /**
     * 院内工号/业务编号
     */
    private String busNo;

    /**
     * 医保人员编码
     */
    private String ybNo;

    /**
     * 关联平台用户主键（与 id 独立，无库表外键）
     */
    private String userId;

    /**
     * 默认登录科室/当前工作科室（Organization 主键）
     */
    private String orgId;

    /**
     * 职称编码
     */
    private Integer drProfttlCode;

    /**
     * 医师/药师执业资格证编号
     */
    private String pharPracCertNo;

    /**
     * 执业证件类型编码
     */
    private String prscDrCertCode;

    /**
     * 医生签名图片 Base64（可含 data:image/*;base64, 前缀）
     */
    private String signature;

    /**
     * 开票点编码
     */
    private String kpdCode;

    /**
     * POS 机终端编号
     */
    private String posNo;
}
