package com.zeroone.star.pharmacy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 工作流 inventory item
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
@Getter
@Setter
@ToString
@TableName("wkf_inventory_item")
public class WkfInventoryItem implements Serializable {

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
     * 库存项聚合根（DM-WKF-008）
     */
    private String categoryCode;

    /**
     * 物品定义表名
     */
    private String itemTable;

    /**
     * 物品定义标识
     */
    private String itemId;

    /**
     * 物品名称（冗余）
     */
    private String name;

    /**
     * 拼音码
     */
    private String pyStr;

    /**
     * 五笔码
     */
    private String wbStr;

    /**
     * 库存状态（ACTIVE/INACTIVE），与 PublicationStatus 编码对齐
     */
    private String inventoryStatusEnum;

    /**
     * 供应商标识
     */
    private String supplierId;

    /**
     * 描述文本
     */
    private String descriptionText;

    /**
     * 计量单位编码
     */
    private String unitCode;

    /**
     * 当前库存数量
     */
    private BigDecimal quantity;

    /**
     * 特征扩展 JSON
     */
    private String characteristicJson;

    /**
     * 产品批号
     */
    private String lotNumber;

    /**
     * 生产日期
     */
    private LocalDateTime productionDate;

    /**
     * 效期
     */
    private LocalDateTime expirationDate;

    /**
     * 有效期月数
     */
    private Integer validityMon;

    /**
     * 货位标识
     */
    private String locationStoreId;

    /**
     * 库房标识
     */
    private String locationId;

    /**
     * 追溯码
     */
    private String traceNo;

    /**
     * 追溯码包装层级
     */
    private Integer packagingLevels;
}
