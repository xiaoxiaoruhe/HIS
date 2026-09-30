// Getter: Lombok会自动生成 getPageIndex() 与 getPageSize() 方法，无需手动编写

package com.zeroone.star.project.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 描述：分页查询父类对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@Getter
@Setter
@ToString
public class PageQuery {
    @Min(value = 1, message = "页码最小值为1")
    @Schema(description = "查询页码", example = "1")
    private long pageIndex;
    @Min(value = 1, message = "条数最小值为1")
    @Schema(description = "查询条数", example = "10")
    private long pageSize;
}
