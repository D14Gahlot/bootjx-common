package com.boot.jx.common.doc;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.common.dto.GroupSessionDto;
import com.boot.jx.mongo.CommonDocInterfaces.DocVersion;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;


@Document(collection = "GROUPS")
@TypeAlias("GroupDoc")
public class GroupDoc implements IDocument, DocVersion {
	@Id
	String groupId;
	@Indexed(unique = true)
	String groupName;
	List<GroupSessionDto> sessions;
	boolean isActive;
	private Long createdStamp;
	private String create_by;
	private Long modifiedStamp;
	private String modified_by;
	@Override
	public void setOldVersions(List<DocVersion> arrayList) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public List<DocVersion> getOldVersions() {
		// TODO Auto-generated method stub
		return null;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public Long getCreatedStamp() {
		return createdStamp;
	}
	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}
	public String getCreate_by() {
		return create_by;
	}
	public void setCreate_by(String create_by) {
		this.create_by = create_by;
	}
	public Long getModifiedStamp() {
		return modifiedStamp;
	}
	public void setModifiedStamp(Long modifiedStamp) {
		this.modifiedStamp = modifiedStamp;
	}
	public String getModified_by() {
		return modified_by;
	}
	public void setModified_by(String modified_by) {
		this.modified_by = modified_by;
	}
	public List<GroupSessionDto> getSessions() {
		return sessions;
	}
	public void setSessions(List<GroupSessionDto> sessions) {
		this.sessions = sessions;
	}
	
}


