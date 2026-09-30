package com.zeroone.star.project.dto.pharmacy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "操作人员DTO")
public class OperatorDTO {

    @Schema(description = "供应请求聚合根（同表多行：同一 { bus_no} 聚合多行明细，共享审批状态）", example = "ABU2026082001")
    @NotNull(message = "供应请求聚合根不能为空")
    private String busNo;

    @Schema(description = "操作人员ID(制单人id)", example = "322932096091820032")
    @NotNull(message = "操作人员ID不能为空")
    private String createdBy;
}
