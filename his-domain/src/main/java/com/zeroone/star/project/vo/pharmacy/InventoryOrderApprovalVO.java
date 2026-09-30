package com.zeroone.star.project.vo.pharmacy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "盘点单审批VO")
public class InventoryOrderApprovalVO {

    @Schema(description = "业务单据号", example = "ABU2026082001")
    private String busNo;

    @Schema(description = "制单人ID", example = "322932096091820032")
    private String createdBy;

    @Schema(description = "制单人名称", example = "库管")
    private String createdName;

    @Schema(description = "审批人ID", example = "322932010666430464")
    private String approverId;

    @Schema(description = "审批人名称", example = "药房")
    private String approverName;

    @Schema(description = "审批状态", example = "APPROVED")
    private String statusEnum;

    @Schema(description = "创建时间", example = "2026-08-20 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "审批时间", example = "2026-08-20 11:24:35")
    private LocalDateTime updatedAt;

    @Schema(description = "总金额", example = "1280.50")
    private BigDecimal totalPrice;
}
