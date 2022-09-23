package com.amu.ent.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MenuJson {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("name")
	private String name;

	@JsonProperty("position")
	private Integer position;

	@JsonProperty("applicationsJsonId")
	private List<Long> applicationsJsonId;

}
