package com.amu.ent.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ApplicationPermissionJson {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("typeAccess")
	private String typeAccess;

	@JsonProperty("value")
	private String value;

}
