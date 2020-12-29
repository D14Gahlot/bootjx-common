package com.boot.jx.cache.test;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.boot.jx.cache.TxCacheBox;
import com.boot.jx.cache.test.RedisSampleTxCacheBox.RedisSampleData;
import com.boot.jx.dict.UserClient.ClientType;

@Component
public class RedisSampleTxCacheBox extends TxCacheBox<RedisSampleData> {

	public static class RedisSampleData implements Serializable {
		private static final long serialVersionUID = -2178734153442648084L;

		private String appUrl = null;
		private String returnUrl;

		private String appToken = null;
		private String motp = null;

		private ClientType clientType;
		private BigDecimal terminalId;
		private Long createdStamp;

		public RedisSampleData() {
			this.createdStamp = System.currentTimeMillis();
		}

		public String getReturnUrl() {
			return returnUrl;
		}

		public void setReturnUrl(String returnUrl) {
			this.returnUrl = returnUrl;
		}

		public String getAppUrl() {
			return appUrl;
		}

		public void setAppUrl(String appUrl) {
			this.appUrl = appUrl;
		}

		public String getAppToken() {
			return appToken;
		}

		public void setAppToken(String appToken) {
			this.appToken = appToken;
		}

		public String getMotp() {
			return motp;
		}

		public void setMotp(String motp) {
			this.motp = motp;
		}

		public ClientType getClientType() {
			return clientType;
		}

		public void setClientType(ClientType clientType) {
			this.clientType = clientType;
		}

		public BigDecimal getTerminalId() {
			return this.terminalId;
		}

		public void setTerminalId(BigDecimal terminalId) {
			this.terminalId = terminalId;
		}

		public Long getCreatedStamp() {
			return createdStamp;
		}

		public void setCreatedStamp(Long createdStamp) {
			this.createdStamp = createdStamp;
		}

	}

	@Override
	public RedisSampleData getDefault() {
		RedisSampleData sSOModel = new RedisSampleData();
		return sSOModel;
	}

}
