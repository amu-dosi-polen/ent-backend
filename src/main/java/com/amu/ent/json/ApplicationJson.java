package com.amu.ent.json;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ApplicationJson {

	@JsonProperty("id")
	private Long id;

	@JsonProperty("actif")
	private Boolean actif;

	@JsonProperty("dateDebut")
	private Timestamp dateDebut;

	@JsonProperty("dateFin")
	private Timestamp dateFin;
	
	@JsonProperty("fname")
	private String fname;

	@JsonProperty("name")
	private String name;

	@JsonProperty("url")
	private String url;

	@JsonProperty("requete")
	private String requete;

	@JsonProperty("key")
	private String key;

	@JsonProperty("timer")
	private int timer;

	@JsonProperty("tous")
	private Boolean tous;

	@JsonProperty("tousEmployee")
	private Boolean tousEmployee;

	@JsonProperty("tousFaculty")
	private Boolean tousFaculty;

	@Column(name = "tousResearch")
	private Boolean tousResearch;

	@JsonProperty("tousAffiliate")
	private Boolean tousAffiliate;

	@JsonProperty("tousRetired")
	private Boolean tousRetired;

	@JsonProperty("tousEtu")
	private Boolean tousEtu;

	@JsonProperty("tousAlum")
	private Boolean tousAlum;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("icon")
	private String icon;
	
	@JsonProperty("color")
	private String color;
	
	@JsonProperty("action")
	private String action;
	
	@JsonProperty("conditionAffichage")
	private String conditionAffichage;
		
	@JsonProperty("applicationsPermission")
	private List<ApplicationPermissionJson> applicationsPermission;

}
