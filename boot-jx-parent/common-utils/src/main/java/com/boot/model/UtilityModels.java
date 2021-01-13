package com.boot.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class UtilityModels {

	public interface Stringable {
		void fromString(String testString);
	}

	public interface Indexable {
		public String id();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public interface JsonIgnoreUnknown extends Serializable {
	}

	public interface JsonStringify {
		String toJsonString();
	}

	public interface JsonObject {
		default Object jsonObject() {
			return this;
		}
	}
}
