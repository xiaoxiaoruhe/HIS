package com.zeroone.star.project.query.sample;

import com.zeroone.star.project.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 描述：示例分页查询对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author jacobzp
 * @version 1.0.0
 */
@Getter
@Setter
@ToString
@Schema(description = "示例分页查询对象")
public class SampleQuery extends PageQuery {
    /**
     * 姓名
     */
    @Schema(description = "姓名", example = "张三")
    private String name;
}
