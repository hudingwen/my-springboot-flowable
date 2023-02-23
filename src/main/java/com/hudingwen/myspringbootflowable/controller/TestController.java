package com.hudingwen.myspringbootflowable.controller;

import com.alibaba.fastjson2.JSON;
import com.hudingwen.myspringbootflowable.cmd.StartProcessNameProcessInstanceCmd;
import com.hudingwen.myspringbootflowable.entity.MessageModel;
import com.hudingwen.myspringbootflowable.entity.ProcessDefinitionInfo;
import com.hudingwen.myspringbootflowable.entity.ProcessInstanceInfo;
import com.hudingwen.myspringbootflowable.entity.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.form.FormProperty;
import org.flowable.engine.form.TaskFormData;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * ClassName:TestController
 * Package:com.hudingwen.myspringbootflowable.controller
 * Description:描述
 *
 * @Date:2022/12/22 15:37
 * @Author:胡丁文
 * @E-mail:admin@aiwanyun.cn
 **/
@RestController
@Slf4j
@RequestMapping(value = "/api/test")
public class TestController {
    @Autowired
    RepositoryService repositoryService;

    @Autowired
    ManagementService managementService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private FormService formService;


    /**
     * 这是一个测试方法
     * @return
     */
    @GetMapping(value = "/test")
    public Date testt(){
        //@GetMapping(value = "test",produces= MediaType.APPLICATION_JSON_VALUE)
        return  new Date();
    }
    /**
     * 导入流程
     * @return
     */
    @GetMapping(value="/import")
    public Deployment deployment(String name) {
        // 资源路径
        String path = "file/学生请假流程.bpmn20.xml";
        if(name != null && name != "") {
            path = "file/" + name;
        }
        // 创建部署构建器
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        // 添加资源
        deploymentBuilder.addClasspathResource(path);
        // 执行部署
        Deployment deploy = deploymentBuilder.deploy();

        //这里也可以通过数据库读取内容
        //String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><definitions...</definitions>";
        //deploymentBuilder.addString("single-task2.bpmn20.xml", text).deploy();

        //这里可以通过压缩包部署多个
        //String fileName = "path/multi-task.zip";
        //ZipInputStream inputStream = new ZipInputStream(new FileInputStream(fileName));
        //
        //repositoryService.createDeployment()
        //        .name("multi-task.zip")
        //        .addZipInputStream(inputStream)
        //        .deploy();
        return deploy;
    }

    /**
     * 查询流程定义列表, 涉及到 act_re_procdef表，部署成功会新增记录
     */
    @GetMapping(value = "/process")
    public MessageModel process() {
        List<ProcessDefinition> processList = repositoryService.createProcessDefinitionQuery().list();
        List<ProcessDefinitionInfo> ls = new ArrayList<ProcessDefinitionInfo>();
        for(ProcessDefinition processDefinition : processList){
            ProcessDefinitionInfo processDefinitionInfo = new ProcessDefinitionInfo();
            BeanUtils.copyProperties(processDefinition,processDefinitionInfo);
            ls.add(processDefinitionInfo);
            log.info("ProcessDefinition name = {},deploymentId = {}, processid = {}", processDefinition.getName(), processDefinition.getDeploymentId(),processDefinition.getId());
        }
        return MessageModel.Success("获取成功",ls);
    }

    /**
     * 发起流程
     */
    @GetMapping("/launch")
    public MessageModel launch(String processid) {
        // 发起请假
        Map<String, Object> map = new HashMap<>();
//        map.put("day", 12);
//        map.put("studentUser", "小明");
//        ProcessInstance studentLeave = runtimeService.startProcessInstanceById(processid,processid, map);



        ProcessInstance studentLeave = managementService.executeCommand(new StartProcessNameProcessInstanceCmd<ProcessInstance>("xxxxxx采购项目",processid,map));

        Task task = taskService.createTaskQuery().processInstanceId(studentLeave.getId()).singleResult();
        log.info("instanceid = {} ,name = {},curtaskid = {}",studentLeave.getId(),studentLeave.getName(),task.getId());

        ProcessInstanceInfo processDefinitionInfo = new ProcessInstanceInfo();
        BeanUtils.copyProperties(studentLeave,processDefinitionInfo);
        return MessageModel.Success("获取成功",processDefinitionInfo);//MessageModel
    }

    /**
     * 查看某个实例的代办
     */
    @GetMapping("/taskByInstanceid")
    public MessageModel taskByProcessid(String instanceid){
        List<Task> list = taskService.createTaskQuery().processInstanceId(instanceid).list();
        List<TaskInfo> ls = new ArrayList<TaskInfo>();
        for (Task item:list ) {
            TaskInfo taskInfo = new TaskInfo();
            BeanUtils.copyProperties(item,taskInfo);
            ls.add(taskInfo);
            log.info("taskid = {}, name = {}, instanceid = {}",item.getId(),item.getName(),item.getProcessInstanceId());
        }
        return MessageModel.Success("获取成功",ls);//MessageModel
    }

    /**
     * 查看某个流程定义的流程图
     * @param httpServletResponse
     * @param processid
     * @throws Exception
     */
    @GetMapping(value = "/imageByProcessid")
    public void imageByProcessid(HttpServletResponse httpServletResponse, String processid) throws Exception {
        // 设置响应的类型格式为图片格式
        httpServletResponse.setContentType("image/png");
        //禁止图像缓存。
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setHeader("Cache-Control", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processid);
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", Collections.emptyList(), Collections.emptyList(), "宋体", "宋体", "宋体", this.getClass().getClassLoader(), 1.0, true);
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 查看某个流程走到哪里了的流程图
     * @param httpServletResponse
     * @param instanceid
     * @throws Exception
     */
    @GetMapping( value = "/imageByInstanceid")
    public void imageByInstanceid(HttpServletResponse httpServletResponse, String instanceid) throws Exception {
        // 设置响应的类型格式为图片格式
        httpServletResponse.setContentType("image/png");
        //禁止图像缓存。
        httpServletResponse.setHeader("Pragma", "no-cache");
        httpServletResponse.setHeader("Cache-Control", "no-cache");
        httpServletResponse.setDateHeader("Expires", 0);

        ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(instanceid).singleResult();

        //流程走完的不显示图
        if (pi == null) {
            return;
        }
//        Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
//        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
//        String InstanceId = task.getProcessInstanceId();
//        List<Execution> executions = runtimeService
//                .createExecutionQuery()
//                .processInstanceId(InstanceId)
//                .list();



        //得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<>();
        List<String> flows = new ArrayList<>();
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(instanceid);
        activityIds.addAll(activeActivityIds);


        List<ActivityInstance> highLightedFlowInstances = runtimeService.createActivityInstanceQuery().activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).processInstanceId(instanceid).list();
        for (ActivityInstance exe : highLightedFlowInstances) {
            flows.add(exe.getActivityId());
        }

        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
        ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, "宋体", "宋体", "宋体", this.getClass().getClassLoader(), 1.0, true);

        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth = 0;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 审批(只管指令)
     */
    @GetMapping("/excute")
    public MessageModel excute(String instanceid) {
        Task task = taskService.createTaskQuery().processInstanceId(instanceid).singleResult();
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("outcome", "通过");
        taskService.complete(task.getId(),myMap);
        Task curTask = taskService.createTaskQuery().processInstanceId(instanceid).singleResult();

        TaskInfo taskInfo = new TaskInfo();
        BeanUtils.copyProperties(curTask,taskInfo);
        return MessageModel.Success("获取成功",taskInfo);//MessageModel
    }

    /**
     * 会签之前提交要会签的人
     */
    @GetMapping("/signBefore")
    public MessageModel signBefore(String instanceid) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(instanceid)
                .singleResult();
        Map<String, Object> myMap = new HashMap<>();
        ArrayList<String> strings = new ArrayList<>();
        strings.add("小明1");
        strings.add("小明2");
        strings.add("小明3");
        strings.add("小明4");
        myMap.put("managerUserIds",  strings);
        
        taskService.complete(task.getId(),myMap);
        List<Task> list = taskService.createTaskQuery().processInstanceId(instanceid).list();

        List<TaskInfo> ls = new ArrayList<TaskInfo>();
        for (Task item:list ) {
            TaskInfo taskInfo = new TaskInfo();
            BeanUtils.copyProperties(item,taskInfo);
            ls.add(taskInfo);
            log.info("taskid = {}, name = {}, instanceid = {}",item.getId(),item.getName(),item.getProcessInstanceId());
        }
        
        return MessageModel.Success("获取成功",ls);//MessageModel
    }
    
    /**
     * 会签
     */
    @GetMapping("/excutesign")
    public MessageModel excute(String instanceid,String outcome,String assignee) {
        Object task_approved_count = runtimeService.getVariable(instanceid, "task_approved_count");
        Object task_rejected_count = runtimeService.getVariable(instanceid, "task_rejected_count");
        Task task = taskService.createTaskQuery()
                .processInstanceId(instanceid)
                .taskAssignee(assignee)
                .singleResult();
        Map<String, Object> myMap = new HashMap<>();
        if("通过".equals(outcome))
        {
            //部门会签通过
            if(task_approved_count==null){
                task_approved_count = 1;
            }else{
                task_approved_count = Integer.parseInt(task_approved_count.toString()) + 1;
            }
        }else{
            //部门会签驳回
            if(task_rejected_count==null){
                task_rejected_count = 1;
            }else{
                task_rejected_count = Integer.parseInt(task_rejected_count.toString()) + 1;
            }
        }
        if(task_approved_count ==null) task_approved_count = 0;
        if(task_rejected_count == null) task_rejected_count =0;
        myMap.put("task_approved_count",task_approved_count);
        myMap.put("task_rejected_count",task_rejected_count);
        taskService.complete(task.getId(),myMap);

        List<Task> list = taskService.createTaskQuery().processInstanceId(instanceid).list();

        List<TaskInfo> ls = new ArrayList<TaskInfo>();
        for (Task item:list ) {
            TaskInfo taskInfo = new TaskInfo();
            BeanUtils.copyProperties(item,taskInfo);
            ls.add(taskInfo);
            log.info("taskid = {}, name = {}, instanceid = {}",item.getId(),item.getName(),item.getProcessInstanceId());
        }

        return MessageModel.Success("获取成功",ls);//MessageModel
    }

    /**
     * 老师审批
     */
    @GetMapping("/excute2")
    public MessageModel excute2(String instanceid,String outcome){
        // 老师审批
        Task task = taskService.createTaskQuery().processInstanceId(instanceid).taskCandidateGroup("teacher").singleResult();
        Map<String, Object> teacherMap = new HashMap<>();
        if(outcome != null && outcome !=""){
            teacherMap.put("outcome", outcome);
        }else{
            teacherMap.put("outcome", "通过");
        }

        taskService.complete(task.getId(), teacherMap);

        Task curTask = taskService.createTaskQuery().processInstanceId(instanceid).singleResult();
        TaskInfo taskInfo = new TaskInfo();
        BeanUtils.copyProperties(curTask,taskInfo);
        return MessageModel.Success("获取成功",taskInfo);//MessageModel
    }

    /**
     * 校长审批
     */
    @GetMapping("/excute3")
    public MessageModel excute3(String instanceid,String outcome){
        // 校长审批
        Task task = taskService.createTaskQuery().processInstanceId(instanceid).taskCandidateGroup("principal").singleResult();
        Map<String, Object> principalMap = new HashMap<>();
        if(outcome != null && outcome !=""){
            principalMap.put("outcome", outcome);
        }else{
            principalMap.put("outcome", "通过");
        }
        taskService.complete(task.getId(), principalMap);

        Task curTask = taskService.createTaskQuery().processInstanceId(instanceid).singleResult();
        TaskInfo taskInfo = new TaskInfo();
        BeanUtils.copyProperties(curTask,taskInfo);
        return MessageModel.Success("获取成功",taskInfo);//MessageModel
    }

    /**
     * 查看某个流程的审批历史
     * @param instanceid
     */
    @GetMapping("/excuteHistory")
    public MessageModel excuteHistory(String instanceid){
        // 查看历史
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceid)
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc()
                .list();
        for (HistoricActivityInstance activity : activities) {
            log.info(activity.getActivityName());
        }
        return MessageModel.Success("获取成功",activities);//MessageModel
    }



    /**
     * 查看当前正在进行的流程实例
     */
    @GetMapping("/instance")
    public MessageModel instance(String processid){
        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().processDefinitionId(processid).list();
        List<ProcessInstanceInfo> ls = new ArrayList<ProcessInstanceInfo>();
        for (ProcessInstance item:list) {
            ProcessInstanceInfo processInstanceInfo = new ProcessInstanceInfo();

            BeanUtils.copyProperties(item,processInstanceInfo);
            ls.add(processInstanceInfo);
            log.info("name = {}, processid = {}, instanceid = {}, deploymentid = {}",item.getName(),item.getProcessDefinitionId(),item.getId(),item.getDeploymentId());

            Task task =  taskService.createTaskQuery().processInstanceId(processInstanceInfo.getId()).list().get(0);

            log.info("taskid = {} ,name = {}",task.getId(),task.getName());
            TaskInfo taskInfo = new TaskInfo();
            BeanUtils.copyProperties(task,taskInfo);
            processInstanceInfo.setCurTask(taskInfo);

            TaskFormData taskFormData = processEngine.getFormService().getTaskFormData(taskInfo.getId());
            List<FormProperty> formProperties = taskFormData.getFormProperties();
            taskInfo.setFormProperties(formProperties);

        }
        return MessageModel.Success("获取成功",ls);//MessageModel
    }
    /**
     * 查看历史所有实例(包括删除的实例)
     */
    @GetMapping("/instanceAll")
    public MessageModel instanceAll(String processid){
        List<HistoricProcessInstance> list = historyService.createHistoricProcessInstanceQuery().processDefinitionId(processid).list();
        for (HistoricProcessInstance item:list) {
            log.info("name = {},processid = {}, instanceid = {}, deploymentid = {}",item.getName(),item.getProcessDefinitionId(),item.getId(),item.getDeploymentId());
        }
        return MessageModel.Success("获取成功",list);//MessageModel
    }

    /**
     * 查看某个所有代办任务
     */
    @GetMapping("/task")
    public MessageModel task(String instanceid){
        List<Task> list = taskService.createTaskQuery().processInstanceId(instanceid).list();
        List<TaskInfo> ls = new ArrayList<TaskInfo>();
        for (Task item:list ) {
            TaskInfo taskInfo = new TaskInfo();
            BeanUtils.copyProperties(item,taskInfo);
            ls.add(taskInfo);
            log.info("taskid = {}, name = {}, instanceid = {}",item.getId(),item.getName(),item.getProcessInstanceId());
        }
        return MessageModel.Success("获取成功",ls);//MessageModel
    }

    /**
     * 删除某个流程定义以及流程下面的实例
     * @param processid
     */
    @GetMapping("/deleteProcess")
    public MessageModel deleteProcess(String processid){

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processid).singleResult();
        // 删除给定的部署和级联删除流程实例、历史流程实例和作业。
        int deleteCount = 0;
        repositoryService.deleteDeployment(processDefinition.getDeploymentId(), true);
        log.info("del deploymentid: {}",processDefinition.getDeploymentId());
        deleteCount+=1;
        return MessageModel.Success("获取成功",deleteCount);//MessageModel
    }

    /**
     * 删除某个流程实例
     * @param instanceid
     */
    @GetMapping("/deleteInstance")
    public MessageModel deleteInstance(String instanceid){
        runtimeService.deleteProcessInstance(instanceid,"删除原因");
        log.info("del instanceid: {}",instanceid);
        return MessageModel.Success("获取成功",1);//MessageModel
    }

    /**
     * 暂停/挂起某个发起的流程
     * @param instanceid
     */
    @GetMapping("pauseInstance")
    public MessageModel pauseInstance (String instanceid){
        runtimeService.suspendProcessInstanceById(instanceid);
        log.info("pause instanceid: {}",instanceid);
        return MessageModel.Success("获取成功",1);//MessageModel
    }

    /**
     * 恢复/激活某个发起的流程
     * @param instanceid
     */
    @GetMapping("/activeInstance")
    public MessageModel activeInstance (String instanceid){
        runtimeService.activateProcessInstanceById(instanceid);
        log.info("active instanceid: {}",instanceid);
        return MessageModel.Success("获取成功",1);//MessageModel
    }
    /**
     * 查询某人可操作的流程
     * @param name
     * @return
     */
    @GetMapping(value="/taskByUser")
    public MessageModel taskByUser(@RequestParam String name) {
        String assignee = name;
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(assignee).list();
        List<TaskInfo> ls = new ArrayList<TaskInfo>();
        for (Task task : tasks) {
            TaskInfo taskInfo = new TaskInfo();
            BeanUtils.copyProperties(task,taskInfo);
            ls.add(taskInfo);
        }
        return MessageModel.Success("获取成功",ls);//MessageModel
    }
    /**
     * 查询候选人可操作的流程
     * @param user
     * @return
     */
    @GetMapping(value="/taskByOtherUser")
    public MessageModel taskByOtherUser(@RequestParam String user) {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateUser(user).list();
        List<TaskInfo> ls = new ArrayList<TaskInfo>();
        for (Task task : tasks) {
            TaskInfo taskInfo = new TaskInfo();
            BeanUtils.copyProperties(task,taskInfo);
            ls.add(taskInfo);
        }
        return MessageModel.Success("获取成功",ls);//MessageModel
    }
    /**
     * 查询某组可操作的流程
     * @param group
     * @return
     */
    @GetMapping(value="/taskByGroup")
    public MessageModel taskByGroup(@RequestParam String group) {
        String assignee = group;
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(assignee).list();
        List<TaskInfo> ls = new ArrayList<TaskInfo>();
        for (Task task : tasks) {
            TaskInfo taskInfo = new TaskInfo();
            BeanUtils.copyProperties(task,taskInfo);
            ls.add(taskInfo);
        }
        return MessageModel.Success("获取成功",ls);//MessageModel
    }

    /**
     * 查看某个任务表单信息
     * @param taskid
     */
    @GetMapping("/formByTaskid")
    public MessageModel formByTaskid(String taskid){
        //当前任务信息
        Task task = taskService.createTaskQuery().taskId(taskid).singleResult();
        log.info("name = {}, id = {}, formkey = {}",task.getName(),task.getId(),task.getFormKey()); 
        
        TaskFormData taskFormData = processEngine.getFormService().getTaskFormData(taskid);
        List<FormProperty> formProperties = taskFormData.getFormProperties();
        for (FormProperty formProperty : formProperties) {
            log.info("formProperty.getId() = {}" ,formProperty.getId());
            log.info("formProperty.getName() = {}",formProperty.getName());
            log.info("formProperty.getValue() = {}" ,formProperty.getValue());
        }
        return MessageModel.Success("获取成功",formProperties);//MessageModel
    }

}
