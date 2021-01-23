package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.admin.dto.AgentResponseDto;
import com.boot.jx.admin.dto.DepartmentRequestDto;
import com.boot.jx.admin.dto.DepartmentResponseDto;
import com.boot.jx.admin.model.Agent;
import com.boot.jx.admin.model.Department;
import com.boot.jx.admin.repository.IAgentRepository;
import com.boot.jx.admin.repository.IDepartmentRepository;
import com.boot.utils.ArgUtil;



@Component
public class AdminManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminManager.class);
	
	
	@Autowired
	IAgentRepository agentRepository; 
	
	@Autowired
	IDepartmentRepository departmentRepository;
	
	
	public List<Agent> saveAgent(Agent agent){
		List<Agent> lstAgent = new ArrayList<Agent>();
		if(agent!=null && agent.getAgent_id()==0) {
			agent.setIsactive("Y");
		}else {
			agent.setAgent_id(agent.getAgent_id());
			agent.setModified_date(new Date());
		}
		
		if(ArgUtil.is(agent)) {
			agentRepository.save(agent);
		}
		
		lstAgent =fetchAgentList(null);
		return lstAgent;
	}
	
	
	
	public List<Agent> fetchAgentList(Integer agentId){
		List<Agent> agentList = new  ArrayList<Agent>();
		if(agentId!=null && agentId>0) {
			Agent agent = agentRepository.findOne(agentId);
			agentList.add(agent);
		}else {
			agentList = agentRepository.findAll();
		}
		return agentList;
	}
	
	public  List<Agent> updateAgentStatus(Integer agentId,String status) {
		List<Agent> agentList = new  ArrayList<Agent>();
		if(ArgUtil.is(agentId) && ArgUtil.is(status)) {
			Agent agent = agentRepository.findOne(agentId);
			if(ArgUtil.is(agent)) {
			 agent.setIsactive(status);
			 agentRepository.save(agent);
			}
		}
		agentList=fetchAgentList(null);
		return agentList;
	}
	
	
	
	public List<Department> createAndUpdateDepartment(Department dept){
		List<Department> lstDept = new ArrayList<Department>();
		if(dept!=null && dept.getDept_id()==0) {
			dept.setIsactive("Y");
		}else {
			dept.setDept_id(dept.getDept_id());
			dept.setModified_date(new Date());
		}
		if(ArgUtil.is(dept)) {
			departmentRepository.save(dept);
		}
		lstDept = fetchDept(null);	
		return lstDept;
	}
	
	public List<Department> fetchDept(Integer deptId){
		List<Department> lstDept = new ArrayList<Department>();
		if(deptId!=null && deptId>0) {
			Department dept = departmentRepository.findOne(deptId);
			lstDept.add(dept);
		}else {
			 lstDept = departmentRepository.findAll();
		}
		return lstDept; 
	}
	
	public  List<Department> updateDeptStatus(Integer deptId,String status) {
		List<Department> lstDept = new ArrayList<Department>();
		if(ArgUtil.is(deptId) && ArgUtil.is(status)) {
			Department dept = departmentRepository.findOne(deptId);
			if(ArgUtil.is(dept)) {
			 dept.setIsactive(status);
			 departmentRepository.save(dept);
			}
		}
		lstDept=fetchDept(null);
		return lstDept;
	}
	
	/*public List<AgentResponseDto> saveAgent(AgentRequestDto reqDto){
		List<AgentResponseDto> agentList = new ArrayList<AgentResponseDto>();
		Agent agent = new Agent();
		if(reqDto!=null && reqDto.getAgent_id()==0) {
		agent.setAgent_code(reqDto.getAgent_code());
		agent.setAgent_department(reqDto.getAgent_department());
		agent.setAgent_email(reqDto.getAgent_email());
		agent.setAgent_name(reqDto.getAgent_name());
		agent.setCreated_date(new Date());
		agent.setIsactive("Y");
		}else {
			agent = agentRepository.findOne(reqDto.getAgent_id());
			if(ArgUtil.is(reqDto.getAgent_department())) {
				agent.setAgent_department(reqDto.getAgent_department());
			}
			if(ArgUtil.is(reqDto.getAgent_email())){
				agent.setAgent_email(reqDto.getAgent_email());
			}
			if(ArgUtil.is(reqDto.getAgent_name())){
				agent.setAgent_name(reqDto.getAgent_name());
			}
			agent.setModified_date(new Date());
		}
		if(ArgUtil.is(agent)) {
			agentRepository.save(agent);
		}
		agentList=fetchAgentList(null);
		return agentList;
	}
	*/
	/*public List<AgentResponseDto> fetchAgentList(Integer agentId){
		List<Agent> agentLst = null;
		List<AgentResponseDto> agentList=new ArrayList<AgentResponseDto>();
		if(agentId!=null && agentId>0) {
			Agent agent = agentRepository.findOne(agentId);
			agentList.add(copyAgent(agent));
		}else {
			agentLst = agentRepository.findAll();
			agentList = getAgents(agentLst);
		}
		return agentList;
	}
	*/
	
	/*public  List<AgentResponseDto> updateAgentStatus(Integer agentId,String status) {
		List<AgentResponseDto> agentList=new ArrayList<AgentResponseDto>();
		if(ArgUtil.is(agentId) && ArgUtil.is(status)) {
			Agent agent = agentRepository.findOne(agentId);
			if(ArgUtil.is(agent)) {
			 agent.setIsactive(status);
			 agentRepository.save(agent);
			}
		}
		agentList=fetchAgentList(null);
		return agentList;
	}
	*/
	

	/*public  List<DepartmentResponseDto> updateDeptStatus(Integer deptId,String status) {
		List<DepartmentResponseDto> deptList=new ArrayList<DepartmentResponseDto>();
		if(ArgUtil.is(deptId) && ArgUtil.is(status)) {
			Department dept = departmentRepository.findOne(deptId);
			if(ArgUtil.is(dept)) {
			 dept.setIsactive(status);
			 departmentRepository.save(dept);
			}
		}
		deptList=fetchDept(null);
		return deptList;
	}
	*/
	
	/** create department **/
	/*public List<DepartmentResponseDto> createAndUpdateDepartment(DepartmentRequestDto deptReqDto){
		List<DepartmentResponseDto> lstDept = new ArrayList<DepartmentResponseDto>();
		Department dept = new Department();
		if(deptReqDto!=null && deptReqDto.getDept_id()==0) {
			dept.setDept_code(deptReqDto.getDeptCode());
			dept.setDept_name(deptReqDto.getDeptName());
			dept.setDept_email(deptReqDto.getDeptEmail());
			dept.setCreated_date(new Date());
			dept.setIsactive("Y");
		}else {
			dept = departmentRepository.findOne(deptReqDto.getDept_id());
			if(ArgUtil.is(dept)) {
				if(ArgUtil.is(deptReqDto.getDeptCode())) {
					dept.setDept_code(deptReqDto.getDeptCode());
				}
				if(ArgUtil.is(deptReqDto.getDeptEmail())) {
					dept.setDept_email(deptReqDto.getDeptEmail());
				}
				if(ArgUtil.is(deptReqDto.getDeptName())) {
					dept.setDept_name(deptReqDto.getDeptName());
				}
				dept.setModified_date(new Date());
			}
		}
		
		if(ArgUtil.is(dept)) {
			departmentRepository.save(dept);
		}
		lstDept = fetchDept(null);	
		return lstDept;
	}
	*/
	
	public List<AgentResponseDto> fetchAgent(){
		List<Agent> agent = agentRepository.findAll();
		List<AgentResponseDto> agentList = getAgents(agent);
		return agentList;
	}
	
	/*public List<DepartmentResponseDto> fetchDept(Integer deptId){
		List<DepartmentResponseDto> deptResList =new ArrayList<DepartmentResponseDto>();
		if(deptId!=null && deptId>0) {
			Department dept = departmentRepository.findOne(deptId);
			DepartmentResponseDto deptRes= copyDept(dept);
			deptResList.add(deptRes);
		}else {
			List<Department> lstDept = departmentRepository.findAll();
			deptResList = getDepartmets(lstDept);
		}
		return deptResList; 
	}
	*/
	
	
	
	
	public List<AgentResponseDto> getAgents(List<Agent> agentList){
		List<AgentResponseDto> agentLst = new ArrayList<AgentResponseDto>();
		try {
			 for(Agent agent : agentList) {
				 AgentResponseDto resdto = copyAgent(agent);
				 agentLst.add(resdto);
			 }
		} catch (Exception e) {
			LOGGER.debug("bene list display", e);
		}
		return agentLst;
	}
	
	public List<DepartmentResponseDto> getDepartmets(List<Department> lstDept){
		List<DepartmentResponseDto> deptLst = new ArrayList<DepartmentResponseDto>();
		try {
			for(Department dept : lstDept) {
				DepartmentResponseDto dto = copyDept(dept);
				deptLst.add(dto);
			}
		}catch (Exception e) {
			LOGGER.debug("dept list display", e);
		}
		return deptLst;
	}
	
	
	
	
		
	public AgentResponseDto copyAgent(Agent agent){
		AgentResponseDto dto = new AgentResponseDto();
		try {
			 BeanUtils.copyProperties(dto, agent);
		} catch (Exception e) {
			LOGGER.debug("agent list display", e);
		}
		
		return dto;
	}
	
	
	public DepartmentResponseDto copyDept(Department dept) {
		DepartmentResponseDto dto = new DepartmentResponseDto();
		try {
			 BeanUtils.copyProperties(dto, dept);
		}catch(Exception e) {
			LOGGER.debug("dept list display", e);
			
		}
		return dto;
	}
}
