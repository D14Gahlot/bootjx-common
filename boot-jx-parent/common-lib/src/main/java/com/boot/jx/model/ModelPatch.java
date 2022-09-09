package com.boot.jx.model;

import java.io.Serializable;

import com.boot.model.MapModel.MapEntry;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;

public class ModelPatch implements Serializable, JsonIgnoreUnknown {
	private static final long serialVersionUID = -3704316273095128587L;

	public static enum ModelPatchCommand {
		SET, ADD, DELETE, REMOVE
	}

	String id;

	ModelPatchCommand command;

	String field;

	Object value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ModelPatchCommand getCommand() {
		return command;
	}

	public void setCommand(ModelPatchCommand command) {
		this.command = command;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public MapEntry value() {
		return new MapEntry(value);
	}

}
