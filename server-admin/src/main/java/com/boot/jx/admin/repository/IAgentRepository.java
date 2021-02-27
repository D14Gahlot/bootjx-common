package com.boot.jx.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.boot.jx.admin.model.Agent;

public interface IAgentRepository extends JpaRepository<Agent, Integer> {

	@Query("select a from Agent a where a.agent_code=?1 and a.isactive >= ?2")
	Agent getAgentByCodeAndStatus(String agentCode, String status);
}
