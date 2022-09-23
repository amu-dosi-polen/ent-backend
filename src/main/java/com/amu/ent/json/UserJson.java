package com.amu.ent.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UserJson {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("uid")
	private String uid;

	@JsonProperty("name")
	private String name;

	@JsonProperty("firstname")
	private String firstname;
	
	@JsonProperty("role")
	private String role;
	
}
