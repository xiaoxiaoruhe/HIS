package com.zeroone.star.pharmacy.service.impl;

import com.zeroone.star.pharmacy.entity.WkfInventoryItem;
import com.zeroone.star.pharmacy.mapper.WkfInventoryItemMapper;
import com.zeroone.star.pharmacy.service.IWkfInventoryItemService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zeroone.star.project.dto.PageDTO;
import com.zeroone.star.project.query.pharmacy.InventoryItemQuery;
import com.zeroone.star.project.vo.pharmacy.InventoryItemVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 工作流 inventory item 服务实现类
 * </p>
 *
 * @author tsfmn
 * @since 2026-08-13
 */
@Service
public class WkfInventoryItemServiceImpl extends ServiceImpl<WkfInventoryItemMapper, WkfInventoryItem> implements IWkfInventoryItemService {

    @Override
    public PageDTO<InventoryItemVO> queryInventoryPage(InventoryItemQuery query) {
        Page<WkfInventoryItem> page = new Page<>(query.getPageIndex(), query.getPageSize());
        LambdaQueryWrapper<WkfInventoryItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(WkfInventoryItem::getName, query.getName());
        return PageDTO.create(this.page(page, wrapper), InventoryItemVO::new);
    }
}
