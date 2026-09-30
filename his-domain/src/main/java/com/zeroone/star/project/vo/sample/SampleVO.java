package com.zeroone.star.project.vo.sample;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 描述：示例显示数据对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author jacobzp
 * @version 1.0.0
 */
@Schema(description = "示例显示数据对象")
@Data
public class SampleVO {
    /**
     * 唯一标识
     */
    @Schema(description = "唯一标识", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /**
     * 姓名
     */
    @Schema(description = "姓名", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 性别
     */
    @Schema(description = "性别", example = "男", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sex;

    /**
     * 年龄
     */
    @Schema(description = "年龄", example = "18", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer age;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "admin")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-07-13 12:00:00")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @Schema(description = "修改人", example = "admin")
    private String updateBy;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间", example = "2026-07-13 12:00:00")
    private LocalDateTime updateTime;
}
