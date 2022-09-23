package com.amu.ent.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.Data;

@Entity
@Table(name = "APPLICATION")
@Data
public class Application implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = -7717669774186229091L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "actif")
	private Boolean actif;

	@Column(name = "fname")
	private String fname;

	@Column(name = "dateDebut")
	private Timestamp dateDebut;

	@Column(name = "dateFin")
	private Timestamp dateFin;

	@Column(name = "name")
	private String name;

	@Column(name = "url")
	private String url;

	@Column(name = "requete")
	private String requete;

	@Column(name = "Entkey")
	private String key;

	@Column(name = "timer")
	private int timer;

	@Column(name = "description")
	private String description;

	@Column(name = "icon")
	private String icon;

	@Column(name = "color")
	private String color;

	@Column(name = "action")
	private String action;

	@Column(name = "conditionAffichage")
	private String conditionAffichage;
	
	@Column(name = "tous")
	private Boolean tous;

	@Column(name = "tousEmployee")
	private Boolean tousEmployee;

	@Column(name = "tousFaculty")
	private Boolean tousFaculty;

	@Column(name = "tousResearch")
	private Boolean tousResearch;

	@Column(name = "tousAffiliate")
	private Boolean tousAffiliate;

	@Column(name = "tousRetired")
	private Boolean tousRetired;

	@Column(name = "tousEtu")
	private Boolean tousEtu;

	@Column(name = "tousAlum")
	private Boolean tousAlum;


	@Transient
	Boolean isMandatory;
	
	@Transient
	Boolean IsFavorite;
	
	@Transient
	int postionFavoris;

	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
	private List<ApplicationPermission> applicationsPermission;

	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE, mappedBy = "application")
	private List<CompteurClick> compteursClick;
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.REMOVE, mappedBy = "application")
	private List<Favoris> favorites;

}
