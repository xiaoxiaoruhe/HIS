package com.zeroone.star.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 库存锁定
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
@Getter
@Setter
@ToString
@TableName("inventory_lock")
public class InventoryLock implements Serializable {

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
    private Integer version;

    /**
     * 库存锁定记录（DM-WKF-008 §3.2）
     */
    private String inventoryId;

    /**
     * 锁定标识：true 已锁定、false 未锁定
     */
    private Boolean lockFlag;

    /**
     * 锁定状态：见 LockStatus
     */
    private String status;

    /**
     * 业务单号
     */
    private String busNo;

    /**
     * 同一业务单内的顺序号
     */
    private Integer orderFlag;
}
