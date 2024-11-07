package com.boot.jx.account.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WabaBalanceDto implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	List<WabaDateWiseBalanceDto> dateWiseBaL=new ArrayList<>();
	

	public List<WabaDateWiseBalanceDto> getDateWiseBaL() {
		return dateWiseBaL;
	}
	public void setDateWiseBaL(List<WabaDateWiseBalanceDto> dateWiseBaL) {
		this.dateWiseBaL = dateWiseBaL;
	}
	
	
}
