package com.example.demo.model;

import java.io.Serializable;
import java.util.Date;


import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;

@Entity
@Table (name= "user_content")
public class UserContentModel implements Serializable{
	
	
	
	@EmbeddedId
	private UserContentModelKeys compositeKeys;
	
	@NotBlank
	private String file_description;
	
	@NotBlank
	private String file_name;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date uploaded_on;
	
	public UserContentModel(UserContentModelKeys compositeKeys, String file_description,
			 String file_name, Date uploaded_on, Date updated_on) {
		
		this.compositeKeys = compositeKeys;
		this.file_description = file_description;
		this.file_name = file_name;
		this.uploaded_on = uploaded_on;
		this.updated_on = updated_on;
	}

	@Temporal(TemporalType.TIMESTAMP)
	private Date updated_on;

	public UserContentModel(){
		
	}
	

	public UserContentModelKeys getCompositeKeys() {
		return compositeKeys;
	}

	public void setCompositeKeys(UserContentModelKeys compositeKeys) {
		this.compositeKeys = compositeKeys;
	}

	
	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getFile_description() {
		return file_description;
	}

	public void setFile_description(String file_description) {
		this.file_description = file_description;
	}

	public Date getUploaded_on() {
		return uploaded_on;
	}

	public void setUploaded_on(Date uploaded_on) {
		this.uploaded_on = uploaded_on;
	}

	public Date getUpdated_on() {
		return updated_on;
	}

	public void setUpdated_on(Date updated_on) {
		this.updated_on = updated_on;
	}
	
	
}
