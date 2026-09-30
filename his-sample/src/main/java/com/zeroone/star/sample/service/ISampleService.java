package com.zeroone.star.sample.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zeroone.star.project.dto.PageDTO;
import com.zeroone.star.project.query.sample.SampleQuery;
import com.zeroone.star.project.vo.sample.SampleVO;
import com.zeroone.star.sample.entity.Sample;

/**
 * <p>
 * 演示示	例表 服务类
 * </p>
 *
 * @author jacobzp
 * @since 2026-07-13
 */
public interface ISampleService extends IService<Sample> {

    /**
     * 根据ID查询示例
     * @param id 示例ID
     * @return 示例详情
     */
    SampleVO getById(String id);

    /**
     * 分页查询示例
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageDTO<SampleVO> listPage(SampleQuery query);
}
