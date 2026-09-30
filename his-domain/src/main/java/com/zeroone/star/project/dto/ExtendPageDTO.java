package com.zeroone.star.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <p>
 * 描述：分页数据显示扩展类
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author 阿伟学长
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExtendPageDTO<T> extends PageDTO<T> {
    /**
     * 根据指定的参数创建分页显示的对象
     * @param rows      记录数据
     * @param pageIndex 当前显示码
     * @param pageSize  当前页最大显示条数
     * @param total     数据总数
     */
    public ExtendPageDTO(List<T> rows, long pageIndex, long pageSize, long total) {
        this.rows = rows;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = (long) Math.ceil((double) total / pageSize);
    }

    @Schema(description = "是否有上一页", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    public boolean isPrevious() {
        return this.pageIndex > 1;
    }

    @Schema(description = "是否有下一页", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    public boolean isNext() {
        return this.pageIndex < this.pages;
    }

    @Schema(description = "上一页页码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    public long getPreviousPage() {
        return isPrevious() ? this.pageIndex - 1 : 1;
    }

    @Schema(description = "下一页页码", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    public long getNextPage() {
        return isNext() ? this.pageIndex + 1 : this.pages;
    }

    @Schema(description = "是否是首页", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    public boolean isFirst() {
        return this.pageIndex == 1;
    }

    @Schema(description = "是否是最后一页", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    public boolean isLast() {
        return this.pageIndex.equals(this.pages);
    }
}
