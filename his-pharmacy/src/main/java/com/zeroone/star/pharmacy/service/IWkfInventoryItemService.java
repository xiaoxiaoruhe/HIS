package com.zeroone.star.pharmacy.service;

import com.zeroone.star.pharmacy.entity.WkfInventoryItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zeroone.star.project.dto.pharmacy.ApprovalDTO;
import com.zeroone.star.project.dto.pharmacy.OperatorDTO;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.dto.PageDTO;
import com.zeroone.star.project.query.pharmacy.InventoryItemQuery;
import com.zeroone.star.project.vo.pharmacy.InventoryItemVO;
import com.zeroone.star.project.vo.pharmacy.InventoryOrderApprovalVO;

/**
 * <p>
 * 工作流 inventory item 服务类
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
public interface IWkfInventoryItemService extends IService<WkfInventoryItem> {

    /**
     * 分页查询库存
     * @param query 查询条件
     * @return 库存分页结果
     */
    PageDTO<InventoryItemVO> queryInventoryPage(InventoryItemQuery query);
}
