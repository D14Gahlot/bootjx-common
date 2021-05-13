package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.dto.AgentResponseDto;
import com.boot.jx.admin.dto.DepartmentResponseDto;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.store.AgentStore;
import com.boot.utils.ArgUtil;

@Component
public class AdminManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminManager.class);

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	AgentStore agentStore;

	public List<AgentDoc> saveAgent(AgentDoc agent) {
		List<AgentDoc> lstAgent = new ArrayList<AgentDoc>();
		if (agent != null && (ArgUtil.isEmpty(agent.getAgent_id()) || agent.getAgent_id().equals("0"))) {
			agent.setIsactive("Y");
			agent.setModified_date(null);
			agent.setAgent_id(null);
		} else {
			agent.setAgent_id(agent.getAgent_id());
			agent.setModified_date(new Date());
		}

		if (ArgUtil.isEmpty(agent.getAgent_code()) || ArgUtil.isEmpty(agent.getDept_id())
				|| ArgUtil.isEmpty(agent.getAgent_name())) {
			ApiResponseUtil.throwException("All Inputs Required");
		}

		if (ArgUtil.is(agent)) {
			mongoTemplate.save(agent);
		}

		lstAgent = fetchAgentList(null);
		return lstAgent;
	}

	public List<AgentDoc> fetchAgentList(String agentId) {
		List<AgentDoc> agentList = new ArrayList<AgentDoc>();
		if (ArgUtil.is(agentId)) {
			AgentDoc agent = mongoTemplate.findOne(new Query(Criteria.where("_id").is(agentId)), AgentDoc.class);
			agentList.add(agent);
		} else {
			agentList = mongoTemplate.findAll(AgentDoc.class);
		}
		return agentList;
	}

	public List<AgentDoc> updateAgentStatus(String agentId, String status) {
		List<AgentDoc> agentList = new ArrayList<AgentDoc>();
		if (ArgUtil.is(agentId) && ArgUtil.is(status)) {
			AgentDoc agent = mongoTemplate.findOne(new Query(Criteria.where("_id").is(agentId)), AgentDoc.class);
			if (ArgUtil.is(agent)) {
				agent.setIsactive(status);
				mongoTemplate.save(agent);
			}
		}
		agentList = fetchAgentList(null);
		return agentList;
	}

	public List<AgentDoc> updateAgentAdmin(String agentId) {
		List<AgentDoc> agentList = new ArrayList<AgentDoc>();
		if (ArgUtil.is(agentId)) {
			AgentDoc agent = mongoTemplate.findOne(new Query(Criteria.where("_id").is(agentId)), AgentDoc.class);
			if (ArgUtil.is(agent)) {
				agent.setAdmin(!agent.isAdmin());
				mongoTemplate.save(agent);
			}
		}
		agentList = fetchAgentList(null);
		return agentList;
	}

	public List<DepartmentDoc> createAndUpdateDepartment(DepartmentDoc dept) {
		List<DepartmentDoc> lstDept = new ArrayList<DepartmentDoc>();
		if (dept != null && (ArgUtil.isEmpty(dept.getDept_id()) || dept.getDept_id().equals("0"))) {
			dept.setIsactive("Y");
			dept.setModified_date(null);
			dept.setDept_id(null);
		} else {
			dept.setDept_id(dept.getDept_id());
			dept.setModified_date(new Date());
		}
		if (ArgUtil.is(dept)) {
			if (!ArgUtil.is(dept.getDept_email())) {
				dept.setDept_email(dept.getDept_name() + "@" + AppContextUtil.getTenant());
			}

			if (ArgUtil.isEmpty(dept.getDept_name()) || ArgUtil.isEmpty(dept.getDept_code())) {
				ApiResponseUtil.throwException("All Inputs Required");
			}
			mongoTemplate.save(dept);
		}
		lstDept = fetchDept(null);
		return lstDept;
	}

	public List<DepartmentDoc> fetchDept(String deptId) {
		List<DepartmentDoc> lstDept = new ArrayList<DepartmentDoc>();
		if (ArgUtil.is(deptId)) {
			DepartmentDoc dept = mongoTemplate.findOne(new Query(Criteria.where("_id").is(deptId)),
					DepartmentDoc.class);
			lstDept.add(dept);
		} else {
			lstDept = mongoTemplate.findAll(DepartmentDoc.class);
		}
		return lstDept;
	}

	public List<DepartmentDoc> updateDeptStatus(Integer deptId, String status) {
		List<DepartmentDoc> lstDept = new ArrayList<DepartmentDoc>();
		if (ArgUtil.is(deptId) && ArgUtil.is(status)) {
			DepartmentDoc dept = mongoTemplate.findOne(new Query(Criteria.where("_id").is(deptId)),
					DepartmentDoc.class);
			if (ArgUtil.is(dept)) {
				dept.setIsactive(status);
				mongoTemplate.save(dept);
			}
		}
		lstDept = fetchDept(null);
		return lstDept;
	}

	/*
	 * public List<AgentResponseDto> saveAgent(AgentRequestDto reqDto){
	 * List<AgentResponseDto> agentList = new ArrayList<AgentResponseDto>(); Agent
	 * agent = new Agent(); if(reqDto!=null && reqDto.getAgent_id()==0) {
	 * agent.setAgent_code(reqDto.getAgent_code());
	 * agent.setAgent_department(reqDto.getAgent_department());
	 * agent.setAgent_email(reqDto.getAgent_email());
	 * agent.setAgent_name(reqDto.getAgent_name()); agent.setCreated_date(new
	 * Date()); agent.setIsactive("Y"); }else { agent =
	 * agentRepository.findOne(reqDto.getAgent_id());
	 * if(ArgUtil.is(reqDto.getAgent_department())) {
	 * agent.setAgent_department(reqDto.getAgent_department()); }
	 * if(ArgUtil.is(reqDto.getAgent_email())){
	 * agent.setAgent_email(reqDto.getAgent_email()); }
	 * if(ArgUtil.is(reqDto.getAgent_name())){
	 * agent.setAgent_name(reqDto.getAgent_name()); } agent.setModified_date(new
	 * Date()); } if(ArgUtil.is(agent)) { agentRepository.save(agent); }
	 * agentList=fetchAgentList(null); return agentList; }
	 */
	/*
	 * public List<AgentResponseDto> fetchAgentList(Integer agentId){ List<Agent>
	 * agentLst = null; List<AgentResponseDto> agentList=new
	 * ArrayList<AgentResponseDto>(); if(agentId!=null && agentId>0) { Agent agent =
	 * agentRepository.findOne(agentId); agentList.add(copyAgent(agent)); }else {
	 * agentLst = agentRepository.findAll(); agentList = getAgents(agentLst); }
	 * return agentList; }
	 */

	/*
	 * public List<AgentResponseDto> updateAgentStatus(Integer agentId,String
	 * status) { List<AgentResponseDto> agentList=new ArrayList<AgentResponseDto>();
	 * if(ArgUtil.is(agentId) && ArgUtil.is(status)) { Agent agent =
	 * agentRepository.findOne(agentId); if(ArgUtil.is(agent)) {
	 * agent.setIsactive(status); agentRepository.save(agent); } }
	 * agentList=fetchAgentList(null); return agentList; }
	 */

	/*
	 * public List<DepartmentResponseDto> updateDeptStatus(Integer deptId,String
	 * status) { List<DepartmentResponseDto> deptList=new
	 * ArrayList<DepartmentResponseDto>(); if(ArgUtil.is(deptId) &&
	 * ArgUtil.is(status)) { Department dept = departmentRepository.findOne(deptId);
	 * if(ArgUtil.is(dept)) { dept.setIsactive(status);
	 * departmentRepository.save(dept); } } deptList=fetchDept(null); return
	 * deptList; }
	 */

	/** create department **/
	/*
	 * public List<DepartmentResponseDto>
	 * createAndUpdateDepartment(DepartmentRequestDto deptReqDto){
	 * List<DepartmentResponseDto> lstDept = new ArrayList<DepartmentResponseDto>();
	 * Department dept = new Department(); if(deptReqDto!=null &&
	 * deptReqDto.getDept_id()==0) { dept.setDept_code(deptReqDto.getDeptCode());
	 * dept.setDept_name(deptReqDto.getDeptName());
	 * dept.setDept_email(deptReqDto.getDeptEmail()); dept.setCreated_date(new
	 * Date()); dept.setIsactive("Y"); }else { dept =
	 * departmentRepository.findOne(deptReqDto.getDept_id()); if(ArgUtil.is(dept)) {
	 * if(ArgUtil.is(deptReqDto.getDeptCode())) {
	 * dept.setDept_code(deptReqDto.getDeptCode()); }
	 * if(ArgUtil.is(deptReqDto.getDeptEmail())) {
	 * dept.setDept_email(deptReqDto.getDeptEmail()); }
	 * if(ArgUtil.is(deptReqDto.getDeptName())) {
	 * dept.setDept_name(deptReqDto.getDeptName()); } dept.setModified_date(new
	 * Date()); } }
	 * 
	 * if(ArgUtil.is(dept)) { departmentRepository.save(dept); } lstDept =
	 * fetchDept(null); return lstDept; }
	 */

	public List<AgentResponseDto> fetchAgent() {
		List<AgentDoc> agent = mongoTemplate.findAll(AgentDoc.class);
		List<AgentResponseDto> agentList = getAgents(agent);
		return agentList;
	}

	/*
	 * public List<DepartmentResponseDto> fetchDept(Integer deptId){
	 * List<DepartmentResponseDto> deptResList =new
	 * ArrayList<DepartmentResponseDto>(); if(deptId!=null && deptId>0) { Department
	 * dept = departmentRepository.findOne(deptId); DepartmentResponseDto deptRes=
	 * copyDept(dept); deptResList.add(deptRes); }else { List<Department> lstDept =
	 * departmentRepository.findAll(); deptResList = getDepartmets(lstDept); }
	 * return deptResList; }
	 */

	public List<AgentResponseDto> getAgents(List<AgentDoc> agentList) {
		List<AgentResponseDto> agentLst = new ArrayList<AgentResponseDto>();
		try {
			for (AgentDoc agent : agentList) {
				AgentResponseDto resdto = copyAgent(agent);
				agentLst.add(resdto);
			}
		} catch (Exception e) {
			LOGGER.debug("bene list display", e);
		}
		return agentLst;
	}

	public List<DepartmentResponseDto> getDepartmets(List<DepartmentDoc> lstDept) {
		List<DepartmentResponseDto> deptLst = new ArrayList<DepartmentResponseDto>();
		try {
			for (DepartmentDoc dept : lstDept) {
				DepartmentResponseDto dto = copyDept(dept);
				deptLst.add(dto);
			}
		} catch (Exception e) {
			LOGGER.debug("dept list display", e);
		}
		return deptLst;
	}

	public AgentResponseDto copyAgent(AgentDoc agent) {
		AgentResponseDto dto = new AgentResponseDto();
		try {
			BeanUtils.copyProperties(dto, agent);
		} catch (Exception e) {
			LOGGER.debug("agent list display", e);
		}

		return dto;
	}

	public DepartmentResponseDto copyDept(DepartmentDoc dept) {
		DepartmentResponseDto dto = new DepartmentResponseDto();
		try {
			BeanUtils.copyProperties(dto, dept);
		} catch (Exception e) {
			LOGGER.debug("dept list display", e);

		}
		return dto;
	}


}
