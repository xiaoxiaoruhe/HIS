package com.zeroone.star.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zeroone.star.pharmacy.entity.DocInventoryItemSnapshot;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 库存明细快照静表（DM-DOC-001） Mapper 接口
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
public interface DocInventoryItemSnapshotMapper extends BaseMapper<DocInventoryItemSnapshot> {

    /**
     * 按业务单号物理删除快照（撤回回到草稿后重提会重插快照，唯一索引会冲突，故物理删除）
     * @param busNo 业务单号
     * @return 删除行数
     */
    int deleteByBusNo(@Param("busNo") String busNo);
}
