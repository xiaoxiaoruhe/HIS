package com.zeroone.star.project.dto.sample;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

/**
 * <p>
 * 描述：示例新增数据传输对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author jacobzp
 * @version 1.0.0
 */
@Schema(description = "示例新增数据传输对象")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SampleAddDTO {
    /**
     * 姓名
     */
    @Schema(description = "姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String name;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "男", requiredMode = Schema.RequiredMode.REQUIRED)
    @Pattern(regexp = "[男女]", message = "性别只能是男或女")
    private String sex;

    /**
     * 年龄
     */
    @Schema(description = "年龄", example = "18", requiredMode = Schema.RequiredMode.REQUIRED)
    @Range(min = 0, max = 300, message = "年龄范围在0-300岁之间")
    private Integer age;
}
