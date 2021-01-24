package com.boot.jx.mcq;

import com.boot.utils.UniqueID;

public class Candidate {

	public Candidate() {
		this.id = UniqueID.generateString();
	}

	public String id;

	public boolean leader;

	private long fixedDelay;

	public Candidate fixedDelay(long fixedDelay) {
		this.fixedDelay = fixedDelay;
		return this;
	}

	public long fixedDelay() {
		return this.fixedDelay;
	}

	private long maxAge;

	public Candidate maxAge(long maxAge) {
		this.maxAge = maxAge;
		return this;
	}

	public long maxAge() {
		return this.maxAge;
	}

	private String queue;

	public Candidate queue(String queue) {
		this.queue = queue;
		return this;
	}

	private String tenant;

	public Candidate tenant(String tenant) {
		this.tenant = tenant;
		return this;
	}

	public <T> Candidate queue(Class<T> class1) {
		return this.queue(class1.getName());
	}

	public String queue() {
		return this.queue;
	}

	public boolean isLeader() {
		return leader;
	}

	public void setLeader(boolean leader) {
		this.leader = leader;
	}

	public String getId() {
		return id;
	}

	public String tenant() {
		return tenant;
	}

}