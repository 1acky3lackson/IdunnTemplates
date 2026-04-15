package com.jackyblackson.idunntemplates.backend.commercial.service.withdraw;

import com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw.SystemWithdrawCreateRequest;
import com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw.SystemWithdrawDto;
import com.jackyblackson.idunntemplates.backend.commercial.dto.withdraw.SystemWithdrawStatusUpdateRequest;
import com.jackyblackson.idunntemplates.backend.commercial.entity.withdraw.SystemWithdraw;
import com.jackyblackson.idunntemplates.backend.commercial.repository.withdraw.SystemWithdrawRepository;
import com.jackyblackson.idunntemplates.backend.commercial.service.UserBalanceService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemWithdrawService {

    private final SystemWithdrawRepository systemWithdrawRepository;
    private final UserBalanceService userBalanceService;

    public SystemWithdrawDto toDto(SystemWithdraw entity) {
        SystemWithdrawDto dto = new SystemWithdrawDto();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        dto.setRejectReason(entity.getRejectReason());
        dto.setErrorReason(entity.getErrorReason());
        dto.setTransferProof(entity.getTransferProof());
        dto.setCreateTimeMs(entity.getCreateTimeMs());
        dto.setApproveTimeMs(entity.getApproveTimeMs());
        dto.setRejectTimeMs(entity.getRejectTimeMs());
        dto.setPaidTimeMs(entity.getPaidTimeMs());
        dto.setFinishTimeMs(entity.getFinishTimeMs());
        dto.setErrorTimeMs(entity.getErrorTimeMs());
        return dto;
    }

    public Page<SystemWithdrawDto> getWithdrawalsByUser(String username, Pageable pageable) {
        return systemWithdrawRepository.findByUsername(username, pageable).map(this::toDto);
    }

    public Page<SystemWithdrawDto> getAllWithdrawals(Pageable pageable) {
        return systemWithdrawRepository.findAll(pageable).map(this::toDto);
    }

    public SystemWithdrawDto getWithdrawalById(Long id) {
        return systemWithdrawRepository.findById(id).map(this::toDto)
                .orElseThrow(() -> new IllegalStateException("提现记录未找到: " + id));
    }

    @Transactional
    public SystemWithdrawDto createWithdrawal(String username, SystemWithdrawCreateRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("提现金额必须为正数");
        }

        // 创建提现记录
        SystemWithdraw withdraw = new SystemWithdraw();
        withdraw.setUsername(username);
        withdraw.setAmount(request.getAmount());
        withdraw.setStatus(SystemWithdraw.Status.CREATED);
        withdraw.setCreateTimeMs(System.currentTimeMillis());
        withdraw = systemWithdrawRepository.save(withdraw);

        // 扣减用户余额并记录虚拟点数变动
        userBalanceService.addExpense(username, request.getAmount(), withdraw.getId(), "用户提现");

        return toDto(withdraw);
    }

    @Transactional
    public SystemWithdrawDto updateWithdrawalStatus(Long id, SystemWithdrawStatusUpdateRequest request,
            String currentUser, boolean isAdmin) {
        SystemWithdraw withdraw = systemWithdrawRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("提现记录未找到: " + id));

        SystemWithdraw.Status currentStatus = withdraw.getStatus();
        SystemWithdraw.Status targetStatus;
        try {
            targetStatus = SystemWithdraw.Status.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的提现状态: " + request.getStatus());
        }

        boolean isOwner = withdraw.getUsername().equals(currentUser);

        // a. 用户提出申请 (由 createWithdrawal 处理)
        // b. 管理员审批通过
        if (currentStatus == SystemWithdraw.Status.CREATED && targetStatus == SystemWithdraw.Status.APPROVED) {
            if (!isAdmin)
                throw new IllegalArgumentException("只有管理员才能审批提现");
            withdraw.setStatus(SystemWithdraw.Status.APPROVED);
            withdraw.setApproveTimeMs(System.currentTimeMillis());
        }
        // 补充流程 1. 管理员审批拒绝
        else if (currentStatus == SystemWithdraw.Status.CREATED && targetStatus == SystemWithdraw.Status.REJECTED) {
            if (!isAdmin)
                throw new IllegalArgumentException("只有管理员才能拒绝提现");
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("拒绝提现必须提供理由");
            }
            withdraw.setStatus(SystemWithdraw.Status.REJECTED);
            withdraw.setRejectReason(request.getReason());
            withdraw.setRejectTimeMs(System.currentTimeMillis());

            // 金额退回用户虚拟点数
            userBalanceService.addIncome(withdraw.getUsername(), withdraw.getAmount(), withdraw.getId(),
                    "提现退回: " + request.getReason());
        }
        // c. 管理员转账
        else if (currentStatus == SystemWithdraw.Status.APPROVED && targetStatus == SystemWithdraw.Status.PAID) {
            if (!isAdmin)
                throw new IllegalArgumentException("只有管理员才能确认转账");
            if (request.getTransferProof() == null || request.getTransferProof().trim().isEmpty()) {
                throw new IllegalArgumentException("必须提供转账凭证");
            }
            withdraw.setStatus(SystemWithdraw.Status.PAID);
            withdraw.setTransferProof(request.getTransferProof());
            withdraw.setPaidTimeMs(System.currentTimeMillis());
        }
        // d. 用户收到转账后，在系统中点击确认
        else if (currentStatus == SystemWithdraw.Status.PAID && targetStatus == SystemWithdraw.Status.FINISHED) {
            if (!isOwner && !isAdmin)
                throw new IllegalArgumentException("只有记录所属用户或管理员才能确认收款");
            withdraw.setStatus(SystemWithdraw.Status.FINISHED);
            withdraw.setFinishTimeMs(System.currentTimeMillis());
        }
        // 补充流程 2. 管理员转账后，若用户未收到，则修改为 ERROR 状态
        else if (currentStatus == SystemWithdraw.Status.PAID && targetStatus == SystemWithdraw.Status.ERROR) {
            // 可以由用户或管理员修改为ERROR状态
            if (!isOwner && !isAdmin)
                throw new IllegalArgumentException("只有记录所属用户或管理员才能标记异常");
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("标记异常必须提供说明");
            }
            withdraw.setStatus(SystemWithdraw.Status.ERROR);
            withdraw.setErrorReason(request.getReason());
            withdraw.setErrorTimeMs(System.currentTimeMillis());
        }
        // 补充流程 4. 沟通结果为放弃这次请求 (ERROR -> REJECTED)
        else if (currentStatus == SystemWithdraw.Status.ERROR && targetStatus == SystemWithdraw.Status.REJECTED) {
            if (!isAdmin)
                throw new IllegalArgumentException("只有管理员才能将异常提现标记为拒绝");
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("放弃请求必须提供说明");
            }
            withdraw.setStatus(SystemWithdraw.Status.REJECTED);
            withdraw.setRejectReason(request.getReason());
            withdraw.setRejectTimeMs(System.currentTimeMillis());

            // 金额退回用户虚拟点数
            userBalanceService.addIncome(withdraw.getUsername(), withdraw.getAmount(), withdraw.getId(),
                    "提现退回: " + request.getReason());
        }
        // 补充流程 5. 沟通结果为交易成立 (ERROR -> FINISHED)
        else if (currentStatus == SystemWithdraw.Status.ERROR && targetStatus == SystemWithdraw.Status.FINISHED) {
            if (!isOwner && !isAdmin)
                throw new IllegalArgumentException("只有记录所属用户或管理员才能确认收款");
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new IllegalArgumentException("确认异常交易成立必须提供说明");
            }
            // 记录下解决的理由到 errorReason，或者追加，这里简单覆盖/补充
            withdraw.setErrorReason(withdraw.getErrorReason() + " | 解决说明: " + request.getReason());
            withdraw.setStatus(SystemWithdraw.Status.FINISHED);
            withdraw.setFinishTimeMs(System.currentTimeMillis());
        } else {
            throw new IllegalArgumentException("不合法的状态转换: 从 " + currentStatus + " 到 " + targetStatus);
        }

        withdraw = systemWithdrawRepository.save(withdraw);
        return toDto(withdraw);
    }
}
