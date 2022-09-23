package com.amu.ent.dto;

import java.util.Date;

import com.amu.ent.entity.Application;

import lombok.Data;

@Data
public class FavorisDto {

	private Long id;

	private Date date;

	private Integer position;

	private UserDto user;

	private Application application;

}
