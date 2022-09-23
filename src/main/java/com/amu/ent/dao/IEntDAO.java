package com.amu.ent.dao;
import java.util.List;

import javax.persistence.NoResultException;

import com.amu.ent.auth.user.LdapGroup;
import com.amu.ent.auth.user.LdapPerson;
import com.amu.ent.entity.Application;
import com.amu.ent.entity.ApplicationPermission;
import com.amu.ent.entity.CompteurClick;
import com.amu.ent.entity.Favoris;
import com.amu.ent.entity.Menu;
import com.amu.ent.entity.User;

public interface IEntDAO {

	/**************************************/
	/* Données LDAP                       */
	/**************************************/
	public LdapPerson getInfoUserLdap(String uid);

	public List<LdapGroup> getAllGroups(String groupResearch);

	public List<LdapPerson> getUsersLdap(String uid);
	/**************************************/
	/* Gestion des autorisations */
	/**************************************/
	public Boolean isAuthorized(String uid);

	/**************************************/
	/* Gestion des users		          */
	/**************************************/
	public void createUser(User user);

	public List<User> getAllUsers();

	public User getUserById(long id);

	public User getUserByUid(String uid);

	public int deleteUser(User user);
	
	public void updateUser(User user);
	

	/**************************************/
	/* Gestion des applications           */
	/**************************************/
	public List<Application> getAllApplications();

	public List<Application> getAllApplicationsByGroupsLdapAndUid(List<String> groupsLdap, String uid);
	
	public List<Application> getAllApplicationsTousOrEduPersonAffiliation(Boolean tousEmployee, Boolean tousFaculty, Boolean tousResearch, Boolean tousAffiliate, Boolean tousRetired, Boolean tousEtu, Boolean tousAlum);

	public List<Application> getAllApplicationsByUser(String uid);
	
	public List<Application> getApplicationsFavoritesByUser(String uid);
	
	public Application getApplicationById(Long id);
	
	public List<Application> getAllApplicationsByGroupLdap(String groupLdap);

	public void createApplication(Application application);

	public void deleteApplication(Application application);
	
	public void updateApplication(Application application);

	public List<Application> getApplicationsMoreClickedByUser(String uid);

	public Application getApplicationByFname(String fname);
	/**************************************/
	/* Gestion du menu			          */
	/**************************************/
	public List<Menu> getAllMenus();
	
	public List<Menu> getAllMenusByApplication(Long idApplication);

	public Menu getMenuById(Long id);

	public void addMenu(Menu menu);

	public int deleteMenu(Long id);
	
	public void updateMenu(Menu menu);
	
	/*********************************************/
	/* Gestion des permissions des applications	 */
	/*********************************************/
	public ApplicationPermission getApplicationPermissionById(Long id);

	public void addApplicationPermission(ApplicationPermission applicationPermission);

	public int deleteApplicationPermission(Long id);
	
	public void updateApplicationPermission(ApplicationPermission applicationPermission);
	
	public List<ApplicationPermission> getAllLdapApplicationPermission();
	
	public List<ApplicationPermission> getAllUserApplicationPermission();
	
	/**************************************/
	/* Gestion des favoris			      */
	/**************************************/
	public List<Favoris> getFavorisByUser(String uid);

	public Favoris getFavorisById(Long id);

	public Favoris addFavoris(Favoris favoris);

	//public int deleteFavoris(Favoris favoris);
	public int deleteFavoris(Long id);
	
	public void updateFavoris(Favoris favoris);
	
	//public Favoris getFavorisByUserAndFname(String uid, String fname);
	public Favoris getFavoris(String uid, Long appId);
	
	//public void updateFnameFavoris(String oldFanme, String newFname);
	/**************************************/
	/* Gestion du compteur des clicks     */
	/**************************************/
	//public CompteurClick getCompteurClickByUidAndFname(String uid, String fname);
	//public CompteurClick getCompteurClickByUid(String uid);
	public CompteurClick getCompteurClick(String uid, Long appId);

	public CompteurClick addCompteurClick(CompteurClick compteurClick);

	public int deleteCompteurClick(Long id);
	
	public void updateCompteurClick(CompteurClick compteurClick);
	
	//public void updateFnameCompteurClick(String oldFanme, String newFname);

	/****************************************/

	
}
 