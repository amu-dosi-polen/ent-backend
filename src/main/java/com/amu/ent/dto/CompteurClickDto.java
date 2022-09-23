package com.amu.ent.dto;

import java.util.Date;

import com.amu.ent.entity.Application;

import lombok.Data;

@Data
public class CompteurClickDto	 {

	private Long id;

	private Date clickDate;

	private Integer compteur;

	private UserDto user;

	private Application application;

}
