package com.zeroone.star.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zeroone.star.pharmacy.entity.DocInventoryItemSnapshot;
import com.zeroone.star.pharmacy.entity.InventoryLock;
import com.zeroone.star.pharmacy.entity.WkfInventoryItem;
import com.zeroone.star.pharmacy.entity.WkfSupplyRequest;
import com.zeroone.star.pharmacy.mapper.DocInventoryItemSnapshotMapper;
import com.zeroone.star.pharmacy.mapper.InventoryLockMapper;
import com.zeroone.star.pharmacy.mapper.WkfInventoryItemMapper;
import com.zeroone.star.pharmacy.mapper.WkfSupplyRequestMapper;
import com.zeroone.star.pharmacy.service.IWkfSupplyRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zeroone.star.project.dto.pharmacy.ApprovalDTO;
import com.zeroone.star.project.dto.pharmacy.OperatorDTO;
import com.zeroone.star.project.dto.pharmacy.StocktakeItemDTO;
import com.zeroone.star.project.dto.pharmacy.StocktakeOrderDTO;
import com.zeroone.star.project.vo.pharmacy.InventoryOrderApprovalVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * <p>
 * 工作流 supply request 服务实现类
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WkfSupplyRequestServiceImpl extends ServiceImpl<WkfSupplyRequestMapper, WkfSupplyRequest> implements IWkfSupplyRequestService {

    private final WkfSupplyRequestMapper wkfSupplyRequestMapper;

    private final WkfInventoryItemMapper wkfInventoryItemMapper;

    private final InventoryLockMapper inventoryLockMapper;

    private final DocInventoryItemSnapshotMapper docInventoryItemSnapshotMapper;

    //提交审批流程
    @Transactional(rollbackFor = Exception.class)
    @Override
    public InventoryOrderApprovalVO submitApproval(OperatorDTO operatorDTO) {

        /// 1. 参数校验：检查 operatorDTO 和 busNo 是否为空，空则抛 IllegalArgumentException
        if (operatorDTO == null || operatorDTO.getBusNo() == null) {
            throw new IllegalArgumentException("DTO或者单据号不存在");
        }

        /// 2. 查询明细并校验：按 busNo 查出该单据下所有未删除的明细行
        List<WkfSupplyRequest> allItemsByBusNo = wkfSupplyRequestMapper.selectList(
                new LambdaQueryWrapper<WkfSupplyRequest>()
                        .eq(WkfSupplyRequest::getBusNo, operatorDTO.getBusNo())
                        .eq(WkfSupplyRequest::getIsDeleted, 0));

        /// 校验明细列表不能为空
        if (allItemsByBusNo == null || allItemsByBusNo.isEmpty()) {
            throw new IllegalArgumentException("单据明细不存在");
        }

        /// 校验所有明细的状态必须都是 DRAFT（草稿），否则拒绝提交
        List<String> statusList = allItemsByBusNo.stream()
                .map(WkfSupplyRequest::getStatusEnum)
                .toList();
        boolean isDraft = statusList.stream().allMatch("DRAFT"::equals);
        if (!isDraft) {
            throw new IllegalArgumentException("存在非草稿状态的明细");
        }

        /// 校验 itemId 不能重复，使用 HashSet 去重
        Set<String> set = new HashSet<>();
        for (WkfSupplyRequest item : allItemsByBusNo) {
            if (!set.add(item.getItemId())) {
                throw new IllegalArgumentException("存在重复明细");
            }
        }

        /// 3. 逐行加锁 + 冻结快照
        int orderFlag = 0;
        for (WkfSupplyRequest wr : allItemsByBusNo) {

            /// 查库存：按 itemId + lotNumber + locationId 定位库存记录，不存在则抛异常
            WkfInventoryItem inventory = wkfInventoryItemMapper.selectOne(
                    new LambdaQueryWrapper<WkfInventoryItem>()
                            .eq(WkfInventoryItem::getItemId, wr.getItemId())
                            .eq(WkfInventoryItem::getLotNumber, wr.getLotNumber())
                            .eq(WkfInventoryItem::getLocationId, wr.getPurposeLocationId()));
            if (inventory == null) {
                throw new IllegalArgumentException("当前库存不存在");
            }

            /// 验锁：先查自己是否已加锁，保证重试幂等
            InventoryLock inventoryLock = inventoryLockMapper.selectOne(
                    new LambdaQueryWrapper<InventoryLock>()
                            .eq(InventoryLock::getInventoryId, inventory.getId())
                            .eq(InventoryLock::getBusNo, wr.getBusNo())
                            .eq(InventoryLock::getIsDeleted, false));

            if (inventoryLock == null) {

                /// 再查是否有他人已加锁，防止并发冲突
                Long count = inventoryLockMapper.selectCount(
                        new LambdaQueryWrapper<InventoryLock>()
                                .eq(InventoryLock::getInventoryId, inventory.getId())
                                .ne(InventoryLock::getBusNo, wr.getBusNo())
                                .eq(InventoryLock::getIsDeleted, false));
                if (count > 0) {
                    throw new IllegalArgumentException("该库存已被其他单据锁定");
                }

                /// 加锁：插入一条 InventoryLock 记录，标记 lockFlag=true, status="LOCK"
                InventoryLock newLock = new InventoryLock();
                newLock.setBusNo(wr.getBusNo());
                newLock.setLockFlag(true);
                newLock.setStatus("LOCK");
                newLock.setInventoryId(inventory.getId());
                newLock.setTenantId(inventory.getTenantId());
                newLock.setOrderFlag(orderFlag++);
                inventoryLockMapper.insert(newLock);

                /// 冻结快照：记录提交审批时的库存状态，用于后续审批通过时做差异比对
                LocalDateTime now = LocalDateTime.now();
                DocInventoryItemSnapshot snapshot = new DocInventoryItemSnapshot();
                BeanUtils.copyProperties(inventory, snapshot);
                snapshot.setBusNo(wr.getBusNo());
                snapshot.setSnapshotTime(now);
                snapshot.setCreatedAt(now);
                snapshot.setCreatedBy(operatorDTO.getCreatedBy());
                docInventoryItemSnapshotMapper.insert(snapshot);
            }
        }

        /// 4. 统一改状态：循环外一次性把所有明细从 DRAFT 更新为 PENDING_APPROVAL
        /// 使用 rows == 0 做乐观锁判断，如果已被并发修改则抛 IllegalStateException 触发事务回滚，防止重复提交
        int rows = wkfSupplyRequestMapper.update(null,
                new LambdaUpdateWrapper<WkfSupplyRequest>()
                        .eq(WkfSupplyRequest::getBusNo, operatorDTO.getBusNo())
                        .set(WkfSupplyRequest::getStatusEnum, "PENDING_APPROVAL")
                        .set(WkfSupplyRequest::getUpdatedBy, operatorDTO.getCreatedBy())
                        .set(WkfSupplyRequest::getUpdatedAt, LocalDateTime.now()));
        if (rows == 0) {
            throw new IllegalStateException("单据状态已变更,请刷新后重试");
        }

        /// 5. 构造返回 VO：取第一条明细的 busNo、createdAt，汇总所有明细的 totalPrice
        WkfSupplyRequest first = allItemsByBusNo.get(0);
        InventoryOrderApprovalVO vo = new InventoryOrderApprovalVO();
        vo.setBusNo(first.getBusNo());
        vo.setCreatedBy(operatorDTO.getCreatedBy());
        vo.setStatusEnum("PENDING_APPROVAL");
        vo.setCreatedAt(first.getCreatedAt());

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (WkfSupplyRequest item : allItemsByBusNo) {
            if (item.getTotalPrice() != null) {
                totalPrice = totalPrice.add(item.getTotalPrice());
            }
        }
        vo.setTotalPrice(totalPrice);

        log.info("盘点单提交审批成功: {}", operatorDTO.getBusNo());
        return vo;
    }

    /// 审批通过
    @Override
    public InventoryOrderApprovalVO approveApproval(ApprovalDTO approvalDTO) {
        /// 单据号校验
        if (approvalDTO == null || approvalDTO.getBusNo() == null){
            throw new IllegalArgumentException("单据号不能为空");
        }

        /// 查找单据明细
        List<WkfSupplyRequest> allItemsByBusNo = wkfSupplyRequestMapper.selectList(
                new LambdaQueryWrapper<WkfSupplyRequest>()
                        .eq(WkfSupplyRequest::getBusNo, approvalDTO.getBusNo())
                        .eq(WkfSupplyRequest::getIsDeleted, 0));

        if (allItemsByBusNo == null) {
            throw new IllegalArgumentException("该单据没有明细");
        }

        /// 校验是否为待审批状态
        boolean allMatch = allItemsByBusNo.stream()
                .allMatch(wkfSupplyRequest ->
                        "PENDING_APPROVAL".equals(wkfSupplyRequest.getStatusEnum()));
        if (!allMatch){
            throw new IllegalArgumentException("该状态不是待审批状态");
        }

        for (WkfSupplyRequest wr : allItemsByBusNo){
            /// 取出快照
            DocInventoryItemSnapshot snapshot = docInventoryItemSnapshotMapper.selectOne(
                    new LambdaQueryWrapper<DocInventoryItemSnapshot>()
                            .eq(DocInventoryItemSnapshot::getBusNo, wr.getBusNo())
                            .eq(DocInventoryItemSnapshot::getItemId, wr.getItemId())
                            .eq(DocInventoryItemSnapshot::getIsDeleted, false));
            if (snapshot == null) {
                throw new IllegalStateException("未找到库存快照");
            }
            /// 计算差异 = 实盘数量 - 账面数量
            BigDecimal actualQty = wr.getTotalQuantity() != null ? wr.getTotalQuantity() : BigDecimal.ZERO;
            BigDecimal bookQty = snapshot.getQuantity() != null ? snapshot.getQuantity() : BigDecimal.ZERO;
            BigDecimal diff = actualQty.subtract(bookQty);
            /// 如果不等于则调整
            if (!diff.equals(BigDecimal.ZERO)){
                /// 查询快照对应库存
                WkfInventoryItem inventory= wkfInventoryItemMapper.selectOne(
                        new LambdaQueryWrapper<WkfInventoryItem>()
                                .eq(WkfInventoryItem::getItemId, snapshot.getItemId()));
                if (inventory == null){
                    throw new IllegalArgumentException("库存不存在");
                }

                // 盘盈加库存，盘亏减库存
                inventory.setQuantity(inventory.getQuantity().add(diff));
                inventory.setUpdatedAt(LocalDateTime.now());
                inventory.setUpdatedBy(approvalDTO.getApproverId());
                wkfInventoryItemMapper.updateById(inventory);
            }

            // 4.4 释放锁
            inventoryLockMapper.update(null,
                    new LambdaUpdateWrapper<InventoryLock>()
                            .eq(InventoryLock::getInventoryId, snapshot.getInventoryItemId())
                            .eq(InventoryLock::getBusNo, approvalDTO.getBusNo())
                            .set(InventoryLock::getLockFlag, false)
                            .set(InventoryLock::getStatus, "RELEASED"));
        }

        // 5. 统一改状态为 APPROVED
        int rows = wkfSupplyRequestMapper.update(null,
                new LambdaUpdateWrapper<WkfSupplyRequest>()
                        .eq(WkfSupplyRequest::getBusNo, approvalDTO.getBusNo())
                        .set(WkfSupplyRequest::getStatusEnum, "APPROVED")
                        .set(WkfSupplyRequest::getUpdatedBy, approvalDTO.getApproverId()));
        if (rows == 0) {
            throw new IllegalStateException("单据状态已变更，请刷新后重试");
        }

        // 6. 构造返回 VO
        WkfSupplyRequest first = allItemsByBusNo.get(0);
        InventoryOrderApprovalVO vo = new InventoryOrderApprovalVO();
        vo.setBusNo(first.getBusNo());
        vo.setStatusEnum("APPROVED");

        return vo;
    }

    /// 审批撤回
    @Override
    public InventoryOrderApprovalVO withdrawApproval(OperatorDTO operatorDTO) {

        return null;
    }

    /// 审批驳回
    @Override
    public InventoryOrderApprovalVO rejectApproval(ApprovalDTO approvalDTO) {
        if (approvalDTO == null || approvalDTO.getBusNo() == null){
            throw new IllegalArgumentException("单据不存在");
        }
        /// 查询单据下的所有明细
        List<WkfSupplyRequest> allItems = wkfSupplyRequestMapper.selectList(
                new LambdaQueryWrapper<WkfSupplyRequest>()
                        .eq(WkfSupplyRequest::getBusNo, approvalDTO.getBusNo())
                        .eq(WkfSupplyRequest::getIsDeleted, false));
        if (allItems == null){
            throw new IllegalArgumentException("明细不存在");
        }
        /// 查看明细状态是不是待审批状态
        boolean allMatch = allItems.stream().allMatch(w -> "PENDING_APPROVAL".equals(w.getStatusEnum()));
        if (!allMatch){
            throw new IllegalArgumentException("明细状态不是待审批状态");
        }
        /// 三元组查库存
       for (WkfSupplyRequest item : allItems){
           WkfInventoryItem inventory = wkfInventoryItemMapper.selectOne(
                   new LambdaQueryWrapper<WkfInventoryItem>()
                           .eq(WkfInventoryItem::getItemId, item.getItemId())
                           .eq(WkfInventoryItem::getLotNumber, item.getLotNumber())
                           .eq(WkfInventoryItem::getLocationId, item.getPurposeLocationId()));

           if (inventory == null){
               throw new IllegalArgumentException("库存不存在");
           }
           /// 释放锁
           int unlock = inventoryLockMapper.update(
                   new LambdaUpdateWrapper<InventoryLock>()
                           .eq(InventoryLock::getInventoryId, inventory.getId())
                           .eq(InventoryLock::getBusNo, item.getBusNo())
                           .eq(InventoryLock::getIsDeleted, false)   // 加这个
                           .set(InventoryLock::getLockFlag, false)
                           .set(InventoryLock::getStatus, "UNLOCK")
                           .set(InventoryLock::getIsDeleted,true));

           if (unlock <= 0) {
               throw new IllegalArgumentException("锁释放失败");
           }

           /// 修改单据状态
           int rejected = wkfSupplyRequestMapper.update(null,
                   new LambdaUpdateWrapper<WkfSupplyRequest>()
                           .eq(WkfSupplyRequest::getBusNo, item.getBusNo())
                           .eq(WkfSupplyRequest::getIsDeleted, false)
                           .set(WkfSupplyRequest::getStatusEnum, "REJECTED")
                           .set(WkfSupplyRequest::getReason, approvalDTO.getRemark()));
           if (rejected <= 0){
               throw new IllegalArgumentException("修改单据失败");
           }


       }
        //构造VO
        InventoryOrderApprovalVO vo = new InventoryOrderApprovalVO();
        vo.setApproverId(approvalDTO.getApproverId());
        vo.setBusNo(approvalDTO.getBusNo());
        vo.setStatusEnum(allItems.get(0).getStatusEnum());
        return vo;
    }

    @Override
    public List<InventoryOrderApprovalVO> queryOrderList() {
        return List.of();
    }

    @Override
    public InventoryOrderApprovalVO queryOrderDetail(String busNo) {
        return null;
    }

    @Override
    public String createOrder() {
        return "";
    }

    @Override
    public void saveOrder(StocktakeOrderDTO dto) {

    }

    @Override
    public InventoryOrderApprovalVO saveAndSubmit(StocktakeOrderDTO dto) {
        return null;
    }
}
