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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;

@Entity
@Table(name = "FAVORIS", uniqueConstraints={@UniqueConstraint(name="uid_applicationId", columnNames={"uid", "application_id"})})
@Data
public class Favoris implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9087240029627086210L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "date")
	private Date date;

	@Column(name = "position")
	private Integer position;

	@Column(name = "uid")
	private String uid;

	@ManyToOne (fetch = FetchType.LAZY)
	private Application application;

}
