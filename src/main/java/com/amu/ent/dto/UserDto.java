package com.amu.ent.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "USER")
@Data
public class UserDto implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = -4982785963589676915L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "uid")
	private String uid;

	@Column(name = "name")
	private String name;

	@Column(name = "firstname")
	private String firstname;

	@Column(name = "email")
	private String email;

	@Column(name = "role")
	private String role;
}
