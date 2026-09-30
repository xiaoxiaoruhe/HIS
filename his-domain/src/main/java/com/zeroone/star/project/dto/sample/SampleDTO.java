package com.zeroone.star.project.dto.sample;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * <p>
 * 描述：示例数据传输对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author jacobzp
 * @version 1.0.0
 */
@Schema(description = "示例数据传输对象")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SampleDTO extends SampleAddDTO {
    /**
     * 唯一标识
     */
    @Schema(description = "唯一标识", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "唯一标识不能为空")
    private String id;
}
