package com.zeroone.star.project.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 描述：通用下拉选项（value / label）
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author Jacobzp
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "下拉选项")
public class OptionItemVO {

    @Schema(description = "选项值", example = "322963602326687744")
    private String value;

    @Schema(description = "选项文本", example = "内科病区")
    private String label;
}
