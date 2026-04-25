package com.cloudtakeout.order.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelRuleConfig {

    @PostConstruct
    public void initRules() {
        initFlowRules();
        initDegradeRules();
    }

    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("placeOrderResource");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(2);
        rules.add(flowRule);
        FlowRuleManager.loadRules(rules);
    }

    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource("queryProductResource");
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        degradeRule.setCount(0.5);
        degradeRule.setMinRequestAmount(5);
        degradeRule.setStatIntervalMs(60000);
        degradeRule.setTimeWindow(10);
        rules.add(degradeRule);
        DegradeRuleManager.loadRules(rules);
    }
}
