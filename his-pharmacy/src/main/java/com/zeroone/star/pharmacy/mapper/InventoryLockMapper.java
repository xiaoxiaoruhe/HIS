package com.zeroone.star.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zeroone.star.pharmacy.entity.InventoryLock;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 库存锁定 Mapper 接口
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
public interface InventoryLockMapper extends BaseMapper<InventoryLock> {

    /**
     * 查询某个库存项上当前生效的锁（lock_flag = 1）
     * @param inventoryId 库存项标识
     * @return 锁列表
     */
    List<InventoryLock> findLocksByInventoryId(@Param("inventoryId") String inventoryId);

    /**
     * 释放锁（lock_flag 置 0）
     * @param inventoryId 库存项标识
     * @param busNo 业务单号
     */
    int releaseLock(@Param("inventoryId") String inventoryId,
                    @Param("busNo") String busNo);
}
