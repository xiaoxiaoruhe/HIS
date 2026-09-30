package com.zeroone.star.project.vo.pharmacy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 描述：库存项显示对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author tsfmn
 * @version 1.0.0
 */
@Data
@Schema(description = "库存项显示对象")
public class InventoryItemVO {

    @Schema(description = "库存项标识", example = "INV_TEST_001")
    private String id;

    @Schema(description = "物品定义表名", example = "med_medication_definition")
    private String itemTable;

    @Schema(description = "物品定义标识", example = "MED_TEST_001")
    private String itemId;

    @Schema(description = "物品名称", example = "阿莫西林胶囊")
    private String name;

    @Schema(description = "产品批号", example = "LOT20260801")
    private String lotNumber;

    @Schema(description = "库房标识", example = "WH_TEST_001")
    private String locationId;

    @Schema(description = "当前库存数量", example = "100.00")
    private BigDecimal quantity;

    @Schema(description = "计量单位编码", example = "盒")
    private String unitCode;

    @Schema(description = "供应商标识", example = "SUP_TEST_001")
    private String supplierId;
}
