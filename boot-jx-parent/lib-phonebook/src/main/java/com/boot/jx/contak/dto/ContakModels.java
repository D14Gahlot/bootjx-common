package com.boot.jx.contak.dto;

import java.io.Serializable;

public class ContakModels {

	public static interface ContakEvent {

	}

	public static class ContakActor implements Serializable {

		private static final long serialVersionUID = -3726949939528582247L;
		private String companyId;
		private String phoneId;

		public String getCompanyId() {
			return companyId;
		}

		public void setCompanyId(String companyId) {
			this.companyId = companyId;
		}

		public String getPhoneId() {
			return phoneId;
		}

		public void setPhoneId(String phoneId) {
			this.phoneId = phoneId;
		}

		public ContakActor companyId(String companyId) {
			this.companyId = companyId;
			return this;
		}

		public ContakActor phoneId(String phoneId) {
			this.phoneId = phoneId;
			return this;
		}
	}

	public static class ContakInboundTrigger {
		private String inboundType;
		private ContakActor actor;
		private ContakEvent event;

		public String getInboundType() {
			return inboundType;
		}

		public void setInboundType(String inboundType) {
			this.inboundType = inboundType;
		}

		public ContakActor getActor() {
			if (actor == null)
				this.actor = new ContakActor();
			return actor;
		}

		public void setActor(ContakActor actor) {
			this.actor = actor;
		}

		public ContakEvent getEvent() {
			return event;
		}

		public void setEvent(ContakEvent event) {
			this.event = event;
		}

		public ContakInboundTrigger inboundType(String inboundType) {
			this.inboundType = inboundType;
			return this;
		}

		public ContakInboundTrigger companyId(String companyId) {
			this.getActor().companyId(companyId);
			return this;
		}

		public ContakInboundTrigger phoneId(String phoneId) {
			this.getActor().phoneId(phoneId);
			return this;
		}

		public static ContakInboundTrigger type(String type) {
			return new ContakInboundTrigger().inboundType(type);
		}
	}

}
