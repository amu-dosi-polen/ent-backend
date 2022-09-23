package com.amu.ent.dto;

import java.util.List;

import lombok.Data;

@Data
public class MenuDto {

	private Long id;

	private String name;

	private Integer position;

	private List<ApplicationDto> applications;

}
