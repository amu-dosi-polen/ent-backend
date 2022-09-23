package com.amu.ent.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "MENU")
@Data
public class Menu implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1822228173849807510L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "position")
	private Integer position;

	@ManyToMany
	private List<Application> applications;

}
