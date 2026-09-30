package com.zeroone.star.pharmacy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zeroone.star.pharmacy.entity.WkfSupplyRequest;
import com.zeroone.star.project.vo.pharmacy.InventoryOrderApprovalVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 工作流 supply request Mapper 接口
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
public interface WkfSupplyRequestMapper extends BaseMapper<WkfSupplyRequest> {

    /**
     * 查询当前订单号下所有的明细
     * @param busNo 订单号
     * @return 明细列表
     */
    List<WkfSupplyRequest> findAllItemsByBusNo(@Param("busNo") String busNo);

    /**
     * 提交审批：批量更新同一单据下所有明细状态为待审批，并写入申请人与申请时间
     * @param statusEnum 状态枚举
     * @param applicantId 申请人ID（制单人）
     * @param applyTime 申请时间
     * @param busNo 订单号
     */
    int updateStatusToDraft(@Param("statusEnum") String statusEnum,
                                @Param("applicantId") String applicantId,
                                @Param("applyTime") LocalDateTime applyTime,
                                @Param("busNo") String busNo);

    /**
     * 审批通过：批量更新同一单据下所有明细状态为已通过，并写入审批人与审批时间
     * @param statusEnum 状态枚举
     * @param approverId 审批人ID
     * @param approvalTime 审批时间
     * @param busNo 订单号
     */
    int updateStatusToApproved(@Param("statusEnum") String statusEnum,
                               @Param("approverId") String approverId,
                               @Param("approvalTime") LocalDateTime approvalTime,
                               @Param("busNo") String busNo);

    /**
     * 审批撤回：批量更新同一单据下所有明细状态回草稿，并清空申请人与申请时间
     * @param statusEnum 状态枚举
     * @param busNo 订单号
     */
    int updateStatusToWithdraw(@Param("statusEnum") String statusEnum,
                               @Param("busNo") String busNo);

    /**
     * 审批驳回：批量更新同一单据下所有明细状态为已驳回，并写入审批人、审批时间与驳回原因
     * @param statusEnum 状态枚举
     * @param approverId 审批人ID
     * @param approvalTime 驳回时间
     * @param reason 驳回原因（可空）
     * @param busNo 订单号
     */
    int updateStatusToReject(@Param("statusEnum") String statusEnum,
                             @Param("approverId") String approverId,
                             @Param("approvalTime") LocalDateTime approvalTime,
                             @Param("reason") String reason,
                             @Param("busNo") String busNo);

    /**
     * 查询盘点单列表：按 bus_no 聚合，每个单据一条（含汇总金额）
     * @return 单据列表
     */
    List<InventoryOrderApprovalVO> queryOrderList();

    /**
     * 查询单据中最大单据号
     */
    String selectMaxBusNo();

    /**
     * 删除单据下的草稿明细（覆盖式保存用）
     * @param busNo 单据号
     * @return 删除行数
     */
    int deleteDraftByBusNo(@Param("busNo") String busNo);
}
