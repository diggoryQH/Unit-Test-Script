export interface AdvancedReport {
    totalRevenue: number;
    grossProfit: number;
    orderStats: {
        success: number;
        canceled: number;
        returned: number;
    };
}