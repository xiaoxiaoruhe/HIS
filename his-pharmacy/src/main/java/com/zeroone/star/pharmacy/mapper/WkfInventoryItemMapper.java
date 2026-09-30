package com.zeroone.star.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zeroone.star.pharmacy.entity.WkfInventoryItem;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 工作流 inventory item Mapper 接口
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
public interface WkfInventoryItemMapper extends BaseMapper<WkfInventoryItem> {

    /**
     * 根据三元组查询有关库存项
     * @param itemId 物品定义标识
     * @param lotNumber 批次号
     * @param purposeLocationId 库房标识
     */
    WkfInventoryItem selectAllItemsByBusNo(
            @Param("itemId") String itemId,
            @Param("lotNumber") String lotNumber,
            @Param("purposeLocationId") String purposeLocationId
    );

    /**
     * 更新库存数量
     * @param id 库存项标识
     * @param quantity 新的库存数量
     */
    int updateQuantity(@Param("id") String id,
                       @Param("quantity") BigDecimal quantity);
}
