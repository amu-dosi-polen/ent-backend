package com.amu.ent.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FnameUidJson {

	@JsonProperty("uid")
	private String uid;

	@JsonProperty("fname")
	private String fname;
	
}
