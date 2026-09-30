package com.zeroone.star.project.dto.pharmacy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 描述：盘点单明细数据传输对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author tsfmn
 * @version 1.0.0
 */
@Data
@Schema(description = "盘点单明细DTO")
public class StocktakeItemDTO {

    @Schema(description = "物品定义标识", example = "MED_TEST_001")
    @NotNull(message = "物品标识不能为空")
    private String itemId;

    @Schema(description = "物品定义表名", example = "med_medication_definition")
    private String itemTable;

    @Schema(description = "产品批号", example = "LOT20260801")
    private String lotNumber;

    @Schema(description = "实盘数量", example = "98.00")
    private BigDecimal totalQuantity;

    @Schema(description = "盈亏数量（实盘-账面）", example = "-2.00")
    private BigDecimal itemQuantity;

    @Schema(description = "计量单位编码", example = "盒")
    private String unitCode;

    @Schema(description = "价格", example = "12.50")
    private BigDecimal price;

    @Schema(description = "合计价格", example = "1225.00")
    private BigDecimal totalPrice;
}
