/*
 * JsonVO<String>格式: 
 *   统一的响应包装类，序列化后 JSON 是这三层结构:
 * {
 *   "code": 10000, # 业务状态码，成功默认 10000 Integer
 *   "message": "操作成功", # 业务状态码描述，String
 *   "data": "这里是字符串内容" # 业务数据，String
 * }
 */


package com.zeroone.star.project.sample;

import com.zeroone.star.project.dto.PageDTO;
import com.zeroone.star.project.dto.sample.SampleAddDTO;
import com.zeroone.star.project.dto.sample.SampleDTO;
import com.zeroone.star.project.query.sample.SampleQuery;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.vo.sample.SampleVO;

/**
 * <p>
 * 描述：示例增删改查接口
 * </p>
 * <p>版权：&copy;01星球</p>
 * <p>地址：01星球总部</p>
 * @author jacobzp
 * @version 1.0.0
 */
public interface SampleApis {
    /**
     * 新增示例
     * @param addDto 示例新增数据
     * @return 新增结果（示例ID）
     */
    JsonVO<String> addSample(SampleAddDTO addDto);

    /**
     * 修改示例
     * @param sampleDTO 示例数据
     * @return 修改结果
     */
    JsonVO<String> modifySample(SampleDTO sampleDTO);

    /**
     * 删除示例
     * @param id 示例ID
     * @return 删除结果
     */
    JsonVO<String> removeSample(String id);

    /**
     * 查询示例详情
     * @param id 示例ID
     * @return 示例详情
     */
    JsonVO<SampleVO> querySample(String id);

    /**
     * 分页查询示例
     * @param query 分页查询条件
     * @return 分页结果
     */
    JsonVO<PageDTO<SampleVO>> querySamplePage(SampleQuery query);
}
