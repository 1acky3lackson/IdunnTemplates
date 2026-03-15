import apiClient from "@/lib/axios";
import type { PageResponse } from "~/common/generic-crud-table/generic-crud-table";
import { SystemWithdraw, SystemWithdrawCreateRequest, SystemWithdrawStatusUpdateRequest } from "./generated/model/system-withdraw";

export const SystemWithdrawApi = {
    listAllWithdrawals: async (page: number, size: number, search?: string, sort?: string) => {
        const response = await apiClient.get<PageResponse<SystemWithdraw>>(`/api/v1/commercial/withdrawals`, {
            params: { page, size, search, sort }
        });
        return response.data;
    },

    listMyWithdrawals: async (page: number, size: number, search?: string, sort?: string) => {
        const response = await apiClient.get<PageResponse<SystemWithdraw>>(`/api/v1/commercial/withdrawals/me`, {
            params: { page, size, search, sort }
        });
        return response.data;
    },

    getWithdrawal: async (id: number) => {
        const response = await apiClient.get<SystemWithdraw>(`/api/v1/commercial/withdrawals/${id}`);
        return response.data;
    },

    createWithdrawal: async (request: SystemWithdrawCreateRequest) => {
        const response = await apiClient.post<SystemWithdraw>(`/api/v1/commercial/withdrawals`, request);
        return response.data;
    },

    updateWithdrawalStatus: async (id: number, request: SystemWithdrawStatusUpdateRequest) => {
        const response = await apiClient.patch<SystemWithdraw>(`/api/v1/commercial/withdrawals/${id}/status`, request);
        return response.data;
    }
};
