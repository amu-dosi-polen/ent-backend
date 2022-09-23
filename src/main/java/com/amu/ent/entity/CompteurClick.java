package com.amu.ent.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "COMPTEUR_CLICK")
@Data
public class CompteurClick implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6901394780584353815L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "clickDate")
	private Date clickDate;

	@Column(name = "compteur")
	private Integer compteur;

	@Column(name = "uid")
	private String uid;
	
	@ManyToOne (fetch = FetchType.LAZY)
	private Application application;

}
