package com.zeroone.star.project.dto.pharmacy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 描述：盘点单保存数据传输对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author tsfmn
 * @version 1.0.0
 */
@Data
@Schema(description = "盘点单保存DTO")
public class StocktakeOrderDTO {

    @Schema(description = "单据号", example = "ABU2026082001")
    @NotNull(message = "单据号不能为空")
    private String busNo;

    @Schema(description = "盘点药房（库房标识）", example = "WH_TEST_001")
    private String locationId;

    @Schema(description = "供应类型", example = "STOCKTAKE")
    private String typeEnum;

    @Schema(description = "供应类别", example = "INVENTORY")
    private String categoryEnum;

    @Schema(description = "制单人ID", example = "322932096091820032")
    private String createdBy;

    @Schema(description = "明细列表")
    @NotEmpty(message = "明细不能为空")
    private List<StocktakeItemDTO> items;
}
