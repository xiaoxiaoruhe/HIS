package com.zeroone.star.pharmacy.controller;

import com.zeroone.star.pharmacy.service.IWkfInventoryItemService;
import com.zeroone.star.pharmacy.service.IWkfSupplyRequestService;
import com.zeroone.star.project.dto.pharmacy.ApprovalDTO;
import com.zeroone.star.project.dto.pharmacy.OperatorDTO;
import com.zeroone.star.project.dto.pharmacy.StocktakeOrderDTO;
import com.zeroone.star.project.pharmacy.InventoryOrderApprovalApi;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.vo.pharmacy.InventoryOrderApprovalVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;

import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 工作流 supply request 前端控制器
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
@RestController
@Tag(name = "盘点审批状态机")
@RequestMapping("/pharmacy")
public class WkfSupplyRequestController implements InventoryOrderApprovalApi {

    @Resource
    private IWkfSupplyRequestService service;

    @PutMapping("/submit-approval")
    @Operation(summary = "提交审批")
    @Override
    public JsonVO<InventoryOrderApprovalVO> submitApproval(@Validated @RequestBody OperatorDTO operatorDTO) {
        return JsonVO.success(service.submitApproval(operatorDTO));
    }

    @PutMapping("/approve-approval")
    @Operation(summary = "审批通过")
    @Override
    public JsonVO<InventoryOrderApprovalVO> approveApproval(@Validated @RequestBody ApprovalDTO approvalDTO) {
        return JsonVO.success(service.approveApproval(approvalDTO));
    }

    @PutMapping("/withdraw-approval")
    @Operation(summary = "撤回审批")
    @Override
    public JsonVO<InventoryOrderApprovalVO> withdrawApproval(@Validated @RequestBody OperatorDTO operatorDTO) {
        return JsonVO.success(service.withdrawApproval(operatorDTO));
    }

    @PutMapping("/reject-approval")
    @Operation(summary = "驳回审批")
    @Override
    public JsonVO<InventoryOrderApprovalVO> rejectApproval(@Validated @RequestBody ApprovalDTO approvalDTO) {
        return JsonVO.success(service.rejectApproval(approvalDTO));
    }

    @GetMapping("/order-list")
    @Operation(summary = "获取单据列表")
    @Override
    public JsonVO<List<InventoryOrderApprovalVO>> queryOrderList() {
        return JsonVO.success(service.queryOrderList());
    }

    @GetMapping("/order-detail")
    @Operation(summary = "获取单据详情")
    @Override
    public JsonVO<InventoryOrderApprovalVO> queryOrderDetail(String busNo) {
        return JsonVO.success(service.queryOrderDetail(busNo));
    }

    @PostMapping("/create-order")
    @Operation(summary = "新增单据")
    @Override
    public JsonVO<String> createOrder() {
        return JsonVO.success(service.createOrder());
    }

    @PostMapping("/save-order")
    @Operation(summary = "保存单据")
    @Override
    public JsonVO<String> saveOrder(@Validated @RequestBody StocktakeOrderDTO dto) {
        service.saveOrder(dto);
        return JsonVO.success(dto.getBusNo());
    }

    @PostMapping("/save-and-submit")
    @Operation(summary = "保存并提交审批")
    @Override
    public JsonVO<InventoryOrderApprovalVO> saveAndSubmit(@Validated @RequestBody StocktakeOrderDTO dto) {
        return JsonVO.success(service.saveAndSubmit(dto));
    }
}
