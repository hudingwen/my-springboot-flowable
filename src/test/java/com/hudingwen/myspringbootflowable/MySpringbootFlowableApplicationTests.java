package com.hudingwen.myspringbootflowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class MySpringbootFlowableApplicationTests {

    @Autowired
    RepositoryService repositoryService;
    @Test
    public void testProcessDefinition() {
        List<ProcessDefinition> processList = repositoryService.createProcessDefinitionQuery().list();
        for(ProcessDefinition processDefinition : processList){
            log.info("ProcessDefinition name = {},deploymentId = {},id = {}", processDefinition.getName(), processDefinition.getDeploymentId(),processDefinition.getId());
        }
    }

}
