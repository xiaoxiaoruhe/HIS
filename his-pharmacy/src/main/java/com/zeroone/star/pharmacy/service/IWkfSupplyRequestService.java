package com.zeroone.star.pharmacy.service;

import com.zeroone.star.pharmacy.entity.WkfSupplyRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zeroone.star.project.dto.pharmacy.ApprovalDTO;
import com.zeroone.star.project.dto.pharmacy.OperatorDTO;
import com.zeroone.star.project.dto.pharmacy.StocktakeOrderDTO;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.vo.pharmacy.InventoryOrderApprovalVO;

import java.util.List;

/**
 * <p>
 * 工作流 supply request 服务类
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
public interface IWkfSupplyRequestService extends IService<WkfSupplyRequest> {

    /**
     * 盘点单提交审批
     */
    InventoryOrderApprovalVO submitApproval(OperatorDTO operatorDTO);

    /**
     * 盘点单审批状态通过
     */
    InventoryOrderApprovalVO approveApproval(ApprovalDTO approvalDTO);

    /**
     * 审批撤回
     */
    InventoryOrderApprovalVO withdrawApproval(OperatorDTO operatorDTO);

    /**
     * 审批驳回
     */
    InventoryOrderApprovalVO rejectApproval(ApprovalDTO approvalDTO);

    /**
     * 盘点单列表查询
     */
    List<InventoryOrderApprovalVO> queryOrderList();

    /**
     * 盘点单详情查询
     */
    InventoryOrderApprovalVO queryOrderDetail(String busNo);

    /**
     * 新增单据：生成盘点单号
     */
    String createOrder();

    /**
     * 保存单据：覆盖式写入草稿明细
     */
    void saveOrder(StocktakeOrderDTO dto);

    /**
     * 保存并提交审批：保存草稿后立即提交
     */
    InventoryOrderApprovalVO saveAndSubmit(StocktakeOrderDTO dto);
}
