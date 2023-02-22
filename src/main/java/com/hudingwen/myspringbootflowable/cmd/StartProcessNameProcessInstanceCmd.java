package com.hudingwen.myspringbootflowable.cmd;

import org.flowable.engine.impl.cmd.StartProcessInstanceCmd;

import java.util.Map;

/**
 * ClassName:StartProcessNameProcessInstanceCmd
 * Package:com.hudingwen.myspringbootflowable.cmd
 * Description:描述
 *
 * @Date:2022/12/23 21:16
 * @Author:胡丁文
 * @E-mail:admin@aiwanyun.cn
 **/
public class StartProcessNameProcessInstanceCmd<T> extends StartProcessInstanceCmd<T> {
    public StartProcessNameProcessInstanceCmd(String processInstanceName, String processid, Map<String, Object> variables) {
        super(null, processid, null, variables,null);
        this.processInstanceName = processInstanceName;
    }
}
