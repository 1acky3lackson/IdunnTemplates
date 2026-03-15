export interface SystemWithdraw {
    id: number;
    username: string;
    amount: number;
    status: 'CREATED' | 'APPROVED' | 'REJECTED' | 'PAID' | 'FINISHED' | 'ERROR';
    rejectReason?: string;
    errorReason?: string;
    transferProof?: string;
    createTimeMs: number;
    approveTimeMs?: number;
    rejectTimeMs?: number;
    paidTimeMs?: number;
    finishTimeMs?: number;
    errorTimeMs?: number;
}

export interface SystemWithdrawCreateRequest {
    amount: number;
}

export interface SystemWithdrawStatusUpdateRequest {
    status: string;
    reason?: string;
    transferProof?: string;
}
