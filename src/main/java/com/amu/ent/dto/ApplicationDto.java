package com.amu.ent.dto;

import java.security.Timestamp;

import javax.persistence.Column;

import lombok.Data;

@Data
public class ApplicationDto {

	private Long id;

	private Boolean actif;

	private int cols;

	private Timestamp dateDebut;

	private Timestamp dateFin;

	private int rows;

	private String fname;

	private Boolean alerte = false;

	private int criticite = 0;

	private int compteur = 0;

	private String name;

	private String url;

	private String requete;

	private String key;

	private int timer;

	private String description;

	private String icon;

	private String color;

	private String action;

	private String conditionAffichage;

	private Boolean isMandatory;

	//private Boolean isFiltre;

	private Boolean tous;

	private Boolean tousEmployee;

	private Boolean tousFaculty;

	private Boolean tousResearch;

	private Boolean tousAffiliate;

	private Boolean tousRetired;

	private Boolean tousEtu;

	private Boolean tousAlum;
}
