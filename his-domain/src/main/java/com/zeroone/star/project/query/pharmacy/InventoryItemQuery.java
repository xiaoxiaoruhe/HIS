package com.zeroone.star.project.query.pharmacy;

import com.zeroone.star.project.query.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 描述：库存分页查询对象
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author tsfmn
 * @version 1.0.0
 */
@Getter
@Setter
@ToString
@Schema(description = "库存分页查询对象")
public class InventoryItemQuery extends PageQuery {

    @Schema(description = "物品名称（模糊匹配）", example = "阿莫西林")
    private String name;
}
