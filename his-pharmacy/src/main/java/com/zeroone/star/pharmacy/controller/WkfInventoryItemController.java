package com.zeroone.star.pharmacy.controller;

import com.zeroone.star.pharmacy.service.IWkfInventoryItemService;
import com.zeroone.star.project.dto.PageDTO;
import com.zeroone.star.project.query.pharmacy.InventoryItemQuery;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.vo.pharmacy.InventoryItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 工作流 inventory item 前端控制器
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
@RestController
@Tag(name = "库存项")
@RequestMapping("/pharmacy/wkf-inventory-item")
public class WkfInventoryItemController {

    @Resource
    private IWkfInventoryItemService service;

    @GetMapping("/query-page")
    @Operation(summary = "分页查询库存")
    public JsonVO<PageDTO<InventoryItemVO>> queryInventoryPage(InventoryItemQuery query) {
        return JsonVO.success(service.queryInventoryPage(query));
    }
}
