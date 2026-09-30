package com.zeroone.star.project.dto.pharmacy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "审批人员DTO")
public class ApprovalDTO {

    @Schema(description = "供应请求聚合根（同表多行：同一 { bus_no} 聚合多行明细，共享审批状态）", example = "ABU2026082001")
    @NotNull(message = "供应请求聚合根不能为空")
    private String busNo;

    @Schema(description = "审批人ID", example = "322932010666430464")
    @NotNull(message = "审批人ID不能为空")
    private String approverId;

    @Schema(description = "备注", example = "实盘数量与账面差异较大，请核对后重新提交")
    private String remark;
}
