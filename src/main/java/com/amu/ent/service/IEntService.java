package com.amu.ent.service;

import java.util.List;

import com.amu.ent.auth.user.LdapGroup;
import com.amu.ent.auth.user.LdapPerson;
import com.amu.ent.dto.ApplicationDto;
import com.amu.ent.dto.ApplicationPermissionDto;
import com.amu.ent.dto.ApplicationWithPermissionsAndFiltresDto;
import com.amu.ent.dto.MenuDto;
import com.amu.ent.dto.MenusApplicationsDto;
import com.amu.ent.dto.UserDto;
import com.amu.ent.entity.Favoris;
import com.amu.ent.entity.User;
import com.amu.ent.json.ApplicationJson;
import com.amu.ent.json.ApplicationPermissionJson;
import com.amu.ent.json.CompteurClickJson;
import com.amu.ent.json.FavorisJson;
import com.amu.ent.json.MenuJson;


public interface IEntService {
     
    
	/**************************************/
	/* Données LDAP                       */
	/**************************************/
	LdapPerson getInfoUserLdap(String uid);
	
	List<LdapGroup> getAllGroups(String groupResearch);
	
	List<LdapPerson> getUsersLdap(String uid);
	/**************************************/
	/* Gestion des autorisations         */
	/**************************************/
	Boolean isAuthorized(String uid);
	
	/**************************************/
	/* Gestion des gestionnaires		  */
	/**************************************/
	void createUser(User user);

	List<UserDto> getAllUsers();
	
	User getUserById(long id);

	User getUserByUid(String uid);

	int deleteUser(User user);
	
	void updateUser(User user);	
	
	/**************************************/
	/* Gestion des applications         */
	/**************************************/

	public List<ApplicationWithPermissionsAndFiltresDto> getAllApplications();

	public void createApplication(ApplicationJson applicationJson);

	public void deleteApplication(ApplicationJson applicationJson);
	
	public void updateApplication(ApplicationJson applicationJson);
	
	public List<ApplicationDto> getApplicationsMoreClickedByUser(User user);
	
	public List<ApplicationDto> getApplicationsFavoritesByUser(User user);
	
	public ApplicationDto getApplicationByFname(String fname);

	/**************************************/
	/* Gestion du menu			          */
	/**************************************/
	public MenusApplicationsDto getMenusAndApplicationMandatoryByUser(String uid);
	
	public List<MenuDto> getAllMenus();

	public void addMenu(MenuJson menuJson);

	public int deleteMenu(MenuJson menuJson);
	
	public void updateMenu(MenuJson menuJson);

	public void updateMenuPosition(MenuJson menuJson);

	/*********************************************/
	/* Gestion des permissions des applications	 */
	/*********************************************/
	public void addApplicationPermission(ApplicationPermissionJson applicationPermissionJson);

	public int deleteApplicationPermission(ApplicationPermissionJson applicationPermissionJson);
	
	public void updateApplicationPermission(ApplicationPermissionJson applicationPermissionJson);
	
	public List<ApplicationPermissionDto> getAllLdapApplicationPermission();
	
	public List<ApplicationPermissionDto> getAllUserApplicationPermission();

	/**************************************/
	/* Gestion des favoris			      */
	/**************************************/
	public List<Favoris> getFavorisByUser(User user);

	public void addFavoris(FavorisJson favorisJson);

	public void deleteFavoris(FavorisJson favorisJson);
	
	/**************************************/
	/* Gestion du compteur des clicks     */
	/**************************************/
	public void addCompteurClick(CompteurClickJson compteurClickJson);

	public void deleteCompteurClick(CompteurClickJson compteurClickJson);

}
