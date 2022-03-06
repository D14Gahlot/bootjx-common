package com.boot.jx.common.doc;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.DocVersion;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;

@Document(collection = "AGENTS")
@TypeAlias("AgentDoc")
public class AgentDoc implements IDocument, DocVersion {

    @Id
    private String id;
    @Indexed(unique = true)
    private String agent_code;
    private String agent_name;
    @Indexed(unique = true)
    private String agent_email;
    private String agent_number;
    private String agent_password;
    private String agent_otp;
    private String agent_channels;
    private String authKey;
    private Date created_date;
    private Long createdStamp;
    private String create_by = "ADMIN";
    private Date modified_date;
    private Long modifiedStamp;
    private String modified_by = "ADMIN";
    private String isactive;
    private boolean admin;
    private boolean isSuperAdmin;
    private boolean isDefaultValue;
    private Boolean isEnabled;

    private List<String> channels;
    private String dept_id;

    private List<DocVersion> oldVersions;

    public String getAgent_name() {
	return agent_name;
    }

    public void setAgent_name(String agent_name) {
	this.agent_name = agent_name;
    }

    public String getAgent_email() {
	return agent_email;
    }

    public void setAgent_email(String agent_email) {
	this.agent_email = agent_email;
    }

    public String getAgent_number() {
	return agent_number;
    }

    public void setAgent_number(String agent_number) {
	this.agent_number = agent_number;
    }

    public Date getCreated_date() {
	return created_date;
    }

    public void setCreated_date(Date created_date) {
	this.created_date = created_date;
    }

    public String getCreate_by() {
	return create_by;
    }

    public void setCreate_by(String create_by) {
	this.create_by = create_by;
    }

    public Date getModified_date() {
	return modified_date;
    }

    public void setModified_date(Date modified_date) {
	this.modified_date = modified_date;
    }

    public String getModified_by() {
	return modified_by;
    }

    public void setModified_by(String modified_by) {
	this.modified_by = modified_by;
    }

    public String getIsactive() {
	return isactive;
    }

    public void setIsactive(String isactive) {
	this.isactive = isactive;
    }

    public String getAgent_code() {
	return agent_code;
    }

    public void setAgent_code(String agent_code) {
	this.agent_code = agent_code;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getAgent_password() {
	return agent_password;
    }

    public void setAgent_password(String agent_password) {
	this.agent_password = agent_password;
    }

    public String getAgent_channels() {
	return agent_channels;
    }

    public void setAgent_channels(String agent_channels) {
	this.agent_channels = agent_channels;
    }

    public String getDept_id() {
	return dept_id;
    }

    public void setDept_id(String dept_id) {
	this.dept_id = dept_id;
    }

    public String getAgent_otp() {
	return agent_otp;
    }

    public void setAgent_otp(String agent_otp) {
	this.agent_otp = agent_otp;
    }

    public boolean isAdmin() {
	return admin;
    }

    public void setAdmin(boolean isAdmin) {
	this.admin = isAdmin;
    }

    public boolean isSuperAdmin() {
	return isSuperAdmin;
    }

    public void setSuperAdmin(boolean isSuperAdmin) {
	this.isSuperAdmin = isSuperAdmin;
    }

    public List<String> getChannels() {
	if (!ArgUtil.is(channels)) {
	    channels = StringUtils.toList(agent_channels);
	}
	return channels;
    }

    public void setChannels(List<String> channels) {
	this.channels = channels;
    }

    public boolean isDefaultValue() {
	return isDefaultValue;
    }

    public void setDefaultValue(boolean isDefaultValue) {
	this.isDefaultValue = isDefaultValue;
    }

    public List<DocVersion> getOldVersions() {
	return oldVersions;
    }

    public void setOldVersions(List<DocVersion> oldVersions) {
	this.oldVersions = oldVersions;
    }

    public Long getCreatedStamp() {
	return createdStamp;
    }

    public void setCreatedStamp(Long createdStamp) {
	this.createdStamp = createdStamp;
    }

    public Long getModifiedStamp() {
	return modifiedStamp;
    }

    public void setModifiedStamp(Long modifiedStamp) {
	this.modifiedStamp = modifiedStamp;
    }

    public void setEnabled(boolean isEnabled) {
	this.isEnabled = isEnabled;
    }

    public String getAuthKey() {
	return authKey;
    }

    public void setAuthKey(String authKey) {
	this.authKey = authKey;
    }

    public Boolean getIsEnabled() {
	return ArgUtil.isEqual(isactive, "Y") || ArgUtil.nullAsTrue(isEnabled);
    }

    public void setIsEnabled(Boolean isEnabled) {
	this.isEnabled = isEnabled;
    }

}
