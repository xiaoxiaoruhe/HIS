package com.zeroone.star.project.pharmacy;

import com.zeroone.star.project.dto.pharmacy.ApprovalDTO;
import com.zeroone.star.project.dto.pharmacy.OperatorDTO;
import com.zeroone.star.project.dto.pharmacy.StocktakeOrderDTO;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.vo.pharmacy.InventoryOrderApprovalVO;

import java.util.List;

public interface InventoryOrderApprovalApi {

    /**
     * 盘点单提交审批
     */
    JsonVO<InventoryOrderApprovalVO> submitApproval(OperatorDTO operatorDTO);

    /**
     * 盘点单审批状态通过
     */
    JsonVO<InventoryOrderApprovalVO> approveApproval(ApprovalDTO approvalDTO);

    /**
     * 审批撤回
     */
    JsonVO<InventoryOrderApprovalVO> withdrawApproval(OperatorDTO operatorDTO);

    /**
     * 审批驳回
     */
    JsonVO<InventoryOrderApprovalVO> rejectApproval(ApprovalDTO approvalDTO);

    /**
     * 获取单据列表
     */
    JsonVO<List<InventoryOrderApprovalVO>> queryOrderList();

    /**
     * 获取单据详情
     */
    JsonVO<InventoryOrderApprovalVO> queryOrderDetail(String busNo);

    /**
     * 新增单据
     */
    JsonVO<String> createOrder();

    /**
     * 保存单据
     */
    JsonVO<String> saveOrder(StocktakeOrderDTO dto);

    /**
     * 保存并提交审批
     */
    JsonVO<InventoryOrderApprovalVO> saveAndSubmit(StocktakeOrderDTO dto);
}
