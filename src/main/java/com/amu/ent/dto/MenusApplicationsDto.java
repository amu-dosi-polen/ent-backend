package com.amu.ent.dto;

import java.util.List;

import com.amu.ent.entity.Application;

import lombok.Data;

@Data
public class MenusApplicationsDto {

	private String name;
	private String firstname;
	private String email;
	private String uid;
	private List<MenuDto> menu;
	private List<ApplicationDto> applicationsMandatory;
	private List<ApplicationDto> applicationsFavorites;
	private List<ApplicationDto> applicationsMoreClicked;
}
