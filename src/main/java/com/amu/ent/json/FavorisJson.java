package com.amu.ent.json;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FavorisJson {
//	@JsonProperty("id")
	private Long id;

//	@JsonProperty("date")
	private Date date;

//	@JsonProperty("position")
	private Integer position;

//	@JsonProperty("uid")
	private String uid;

//	@JsonProperty("fname")
	private String fname;
	
//	@JsonProperty("applicationId")
	private Long applicationId;
}
