// Mybatis-Plus 管的是 “Java怎么访问数据库”

package com.zeroone.star.sample.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zeroone.star.project.dto.PageDTO;
import com.zeroone.star.project.query.sample.SampleQuery;
import com.zeroone.star.project.vo.sample.SampleVO;
import com.zeroone.star.sample.entity.Sample;
import com.zeroone.star.sample.mapper.SampleMapper;
import com.zeroone.star.sample.service.ISampleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * <p>
 * 演示示	例表 服务实现类
 * </p>
 *
 * @author jacobzp
 * @since 2026-07-13
 */
@Service
public class SampleServiceImpl extends ServiceImpl<SampleMapper, Sample> implements ISampleService {

    @Override
    public SampleVO getById(String id) {
        Sample sample = super.getById(id);
        if (sample == null) {
            return null;
        }
        return BeanUtil.copyProperties(sample, SampleVO.class);
    }

    @Override
    public PageDTO<SampleVO> listPage(SampleQuery query) {
        // Page<Sample>: 分页容器，里面装的是 Sample 实体列表
        Page<Sample> page = new Page<>(query.getPageIndex(), query.getPageSize());
        // 创建一个查询条件构造器
        LambdaQueryWrapper<Sample> wrapper = new LambdaQueryWrapper<>();
        // 相当于: WHERE name LIKE '%张三%'
        wrapper.like(StringUtils.hasText(query.getName()), Sample::getName, query.getName())
                .orderByDesc(Sample::getCreateTime);
        // 按 wrapper 查询 sample 表, 结果写回传入的 page 对象中
        page(page, wrapper);
        // 把 Page<Sample> 转成对外的 PageDTO<SampleVO>
        return PageDTO.create(page, SampleVO::new);
    }
}
