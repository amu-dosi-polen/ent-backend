package com.amu.ent.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;

@Data
public class ApplicationWithPermissionsAndFiltresDto {

	private Long id;

	private int cols;
	
	private Boolean actif;

	private Timestamp dateDebut;

	private Timestamp dateFin;
	
	private int rows;

	private int timer;
	
	private String name;

	private String fname;

	private String url;

	private String requete;

	private String key;

	private String description;
	
	private String icon;
	
	private String color;
	
	private String action;
	
	private String conditionAffichage;

	private Boolean isMandatory;
	
	private List<ApplicationPermissionDto> applicationsPermission;

	private Boolean tous;

	private Boolean tousEmployee;

	private Boolean tousFaculty;

	private Boolean tousResearch;

	private Boolean tousAffiliate;

	private Boolean tousRetired;

	private Boolean tousEtu;

	private Boolean tousAlum;
	
}
