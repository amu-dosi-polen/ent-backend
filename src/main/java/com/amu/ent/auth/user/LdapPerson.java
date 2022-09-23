package com.amu.ent.auth.user;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class LdapPerson {
	
	private String name;

	private String uid;

	private String firstname;

	private String mail;

	private String eduPersonPrimaryAffiliation;

	private List<String> memberOf;
	
	private List<String> edupersonaffiliation;
	
	public void addMemberOf(String member) {
		if(memberOf == null) {
			memberOf = new ArrayList<String>();
		}
		memberOf.add(member);
	}
	public void addEdupersonaffiliation(String member) {
		if(edupersonaffiliation == null) {
			edupersonaffiliation = new ArrayList<String>();
		}
		edupersonaffiliation.add(member);
	}
}
