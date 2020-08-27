package com.luoluo.delaymq.service.rebalance;

import com.luoluo.delaymq.service.rebalance.strategy.RoundRebalanceImpl;
import com.luoluo.delaymq.service.rebalance.strategy.HashRebalanceImpl;

/**
 * @author luoluo
 * @version 1.0.0
 * @date 2020/07/20
 * @descrn 负载策略枚举
 */
public enum RebalanceStrategyEnum {

    /**
     * 自增取余
     */
    ROUND("自增取余", new RoundRebalanceImpl()),
    /**
     * Hash入队
     */
    HASH("Hash入队",new HashRebalanceImpl())
    ;

    RebalanceStrategyEnum(String description, Rebalance rebalance) {
        this.description = description;
        this.rebalance = rebalance;
    }

    private String description;
    private Rebalance rebalance;

    public String getDescription() {
        return description;
    }

    public Rebalance getRebalance() {
        return rebalance;
    }

    public static Rebalance match(RebalanceStrategyEnum strategyEnum, RebalanceStrategyEnum defaultItem) {
        if (strategyEnum != null) {
            for (RebalanceStrategyEnum item : RebalanceStrategyEnum.values()) {
                if (item.equals(strategyEnum)) {
                    return item.getRebalance();
                }
            }
        }
        return defaultItem.getRebalance();
    }

}
