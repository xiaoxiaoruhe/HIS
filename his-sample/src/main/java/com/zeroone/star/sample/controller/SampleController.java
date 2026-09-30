package com.zeroone.star.sample.controller;

import cn.hutool.core.bean.BeanUtil;
import com.zeroone.star.project.dto.PageDTO;
import com.zeroone.star.project.dto.sample.SampleAddDTO;
import com.zeroone.star.project.dto.sample.SampleDTO;
import com.zeroone.star.project.query.sample.SampleQuery;
import com.zeroone.star.project.sample.SampleApis;
import com.zeroone.star.project.vo.JsonVO;
import com.zeroone.star.project.vo.sample.SampleVO;
import com.zeroone.star.sample.entity.Sample;
import com.zeroone.star.sample.service.ISampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 演示示例表 前端控制器
 * </p>
 *
 * @author jacobzp
 * @since 2026-07-13
 */
@RestController
@Tag(name = "示例接口")
@Validated
public class SampleController implements SampleApis {

    @Resource
    ISampleService service;

    // @PostMapping("/add-sample")：HTTP POST，路径相对应用上下文，完整大致是 /sample/add-sample
    // @Operation：Swagger 文档里的接口说明。
    @PostMapping("/add-sample")
    @Operation(summary = "新增示例")
    @Override
    public JsonVO<String> addSample(@Validated @RequestBody SampleAddDTO addDto) {
        // BeanUtil.copyProperties 做了三件事:
        //   new Sample()
        //   把 addDto 里叫 name / sex / age 的值，赋给 sample 同名字段
        //   返回 sample
        Sample sample = BeanUtil.copyProperties(addDto, Sample.class);
        if (service.save(sample)) {
            return JsonVO.success(sample.getId());
        }
        return JsonVO.fail(null);
    }


    @PutMapping("/modify-sample")
    @Operation(summary = "修改示例")
    @Override
    public JsonVO<String> modifySample(@Validated @RequestBody SampleDTO sampleDTO) {
        Sample sample = BeanUtil.copyProperties(sampleDTO, Sample.class);
        if (service.updateById(sample)) {
            return JsonVO.success(sampleDTO.getId());
        }
        return JsonVO.fail(null);
    }

    // @Parameter(..., in = ParameterIn.QUERY)：告诉文档：id 是 查询参数（写在 URL 上），不是请求体。
    @DeleteMapping("/remove-sample")
    @Operation(summary = "删除示例")
    @Parameter(name = "id", description = "示例ID", required = true, in = ParameterIn.QUERY, example = "1")
    @Override
    public JsonVO<String> removeSample(String id) {
        if (service.removeById(id)) {
            return JsonVO.success(id);
        }
        return JsonVO.fail(null);
    }

    @GetMapping("/query-sample")
    @Operation(summary = "查询示例详情")
    @Parameter(name = "id", description = "示例ID", required = true, in = ParameterIn.QUERY, example = "1")
    @Override
    public JsonVO<SampleVO> querySample(String id) {
        SampleVO vo = service.getById(id);
        if (vo == null) {
            return JsonVO.fail(null);
        }
        return JsonVO.success(vo);
    }

    @GetMapping("/query-sample-page")
    @Operation(summary = "分页查询示例")
    @Override
    public JsonVO<PageDTO<SampleVO>> querySamplePage(@Validated SampleQuery query) {
        return JsonVO.success(service.listPage(query));
    }
}
