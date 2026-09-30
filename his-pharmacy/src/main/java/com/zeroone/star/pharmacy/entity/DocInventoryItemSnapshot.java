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
 * 库存明细快照静表（DM-DOC-001）
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
@Getter
@Setter
@ToString
@TableName("doc_inventory_item_snapshot")
public class DocInventoryItemSnapshot implements Serializable {

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
     * 批次单号（ABU+yyyyMMdd+2位序号）
     */
    private String busNo;

    /**
     * 快照时刻
     */
    private LocalDateTime snapshotTime;

    /**
     * 源库存项标识（写入时刻 wkf_inventory_item.id）
     */
    private String inventoryItemId;

    /**
     * 物品定义表名
     */
    private String itemTable;

    /**
     * 物品定义标识
     */
    private String itemId;

    /**
     * 收费项定义标识
     */
    private String chargeItemDefinitionId;

    /**
     * 库房标识（不含货位）
     */
    private String warehouseId;

    /**
     * 供应商标识
     */
    private String supplierId;

    /**
     * 项目编码
     */
    private String productBusNo;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 商品名称
     */
    private String merchandiseName;

    /**
     * 规格
     */
    private String totalVolume;

    /**
     * 厂家/产地
     */
    private String manufacturer;

    /**
     * 批准文号
     */
    private String approvalNumber;

    /**
     * 医保码
     */
    private String ybNo;

    /**
     * 追溯码
     */
    private String traceNo;

    /**
     * 拼音码（备份关键字/排序）
     */
    private String pyStr;

    /**
     * 五笔码（备份关键字）
     */
    private String wbStr;

    /**
     * 生产批号
     */
    private String lotNumber;

    /**
     * 账面数量（拆零最小单位）
     */
    private BigDecimal quantity;

    /**
     * 拆零↔包装倍数（1包装=ratio拆零）
     */
    private BigDecimal packageUnitRatio;

    /**
     * 包装单位编码
     */
    private String packageUnitCode;

    /**
     * 批号进价（包装整包）
     */
    private BigDecimal lotNumberCostAmount;

    /**
     * 批号售价（包装整包）
     */
    private BigDecimal lotNumberPriceAmount;

    /**
     * 进价行总额（写入时刻冻结）
     */
    private BigDecimal lineCostTotal;

    /**
     * 售价行总额（写入时刻冻结）
     */
    private BigDecimal linePriceTotal;

    /**
     * 生产日期
     */
    private LocalDateTime productionDate;

    /**
     * 效期
     */
    private LocalDateTime expirationDate;

    /**
     * 剩余过期天数（写入时刻冻结）
     */
    private Integer remainingDays;

    /**
     * 供应状态（PublicationStatus code）
     */
    private String inventoryStatusEnum;

    /**
     * 医保等级码
     */
    private Integer chrgitmLv;

    /**
     * 药品分类编码
     */
    private String medicationCategoryCode;

    /**
     * 耗材分类编码
     */
    private String deviceCategoryCode;

    /**
     * 剂型编码
     */
    private String doseFormCode;

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
}
