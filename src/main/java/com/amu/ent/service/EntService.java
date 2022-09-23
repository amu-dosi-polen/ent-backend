package com.amu.ent.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amu.ent.auth.user.LdapGroup;
import com.amu.ent.auth.user.LdapPerson;
import com.amu.ent.controller.EntController;
import com.amu.ent.dao.IEntDAO;
import com.amu.ent.dto.ApplicationDto;
import com.amu.ent.dto.ApplicationPermissionDto;
import com.amu.ent.dto.ApplicationWithPermissionsAndFiltresDto;
import com.amu.ent.dto.MenuDto;
import com.amu.ent.dto.MenusApplicationsDto;
import com.amu.ent.dto.UserDto;
import com.amu.ent.entity.Application;
import com.amu.ent.entity.ApplicationPermission;
import com.amu.ent.entity.CompteurClick;
import com.amu.ent.entity.Favoris;
import com.amu.ent.entity.Menu;
import com.amu.ent.entity.User;
import com.amu.ent.json.ApplicationJson;
import com.amu.ent.json.ApplicationPermissionJson;
import com.amu.ent.json.CompteurClickJson;
import com.amu.ent.json.FavorisJson;
import com.amu.ent.json.MenuJson;

@Service
public class EntService implements IEntService {

	@Autowired
	private IEntDAO entDAO;

	@Autowired
	private ModelMapper modelMapper;

	private static final Logger logger = LoggerFactory.getLogger(EntController.class);

	/**************************************/
	/* Données LDAP */
	/**************************************/
	@Override
	public LdapPerson getInfoUserLdap(String uid) {
		return entDAO.getInfoUserLdap(uid);
	}
	
	@Override
	public List<LdapGroup> getAllGroups(String groupResearch) {
		return entDAO.getAllGroups(groupResearch);
	}
	
	@Override
	public List<LdapPerson> getUsersLdap(String uid) {
		return entDAO.getUsersLdap(uid);
	}
	/**************************************/
	/* Gestion des autorisations */
	/**************************************/
	@Override
	public Boolean isAuthorized(String uid) {
		return entDAO.isAuthorized(uid);
	}

	/**************************************/
	/* Gestion des users */
	/**************************************/
	@Override
	public void createUser(User user) {
		entDAO.createUser(user);
	}

	@Override
	public List<UserDto> getAllUsers() {
		List<UserDto> usersDto = new ArrayList<UserDto>();

		for (User user : entDAO.getAllUsers()) {
			UserDto userDto = modelMapper.map(user, UserDto.class);
			usersDto.add(userDto);
		}
		return usersDto;
	}

	@Override
	public int deleteUser(User user) {
		return entDAO.deleteUser(user);
	}

	@Override
	public void updateUser(User user) {
		entDAO.updateUser(user);
	}

	@Override
	public User getUserById(long id) {
		return entDAO.getUserById(id);
	}

	@Override
	public User getUserByUid(String uid) {
		return entDAO.getUserByUid(uid);
	}

	/**************************************/
	/* Gestion des applications 		  */
	/**************************************/
	@Override
	public void createApplication(ApplicationJson applicationJson) {
		Application application = modelMapper.map(applicationJson, Application.class);
		entDAO.createApplication(application);
	}

	@Override
	public void deleteApplication(ApplicationJson applicationJson) {
		if (applicationJson.getId() != null) {
			Application application = entDAO.getApplicationById(applicationJson.getId());
			// récupération de tous les menus
			List<Menu> menus = entDAO.getAllMenusByApplication(applicationJson.getId());
			for (Menu menu : menus) {
				// suppression de l'application concernée
				menu.getApplications().remove(application);
				entDAO.updateMenu(menu);
			}
			entDAO.deleteApplication(application);
		}
		logger.error("erreur execution du ws delete-application - id null: ");
	}

	@Override
	public void updateApplication(ApplicationJson applicationJson) {
		Application application = entDAO.getApplicationById(applicationJson.getId());

		application = modelMapper.map(applicationJson, Application.class);
		entDAO.updateApplication(application);
	}

	@Override
	public List<ApplicationWithPermissionsAndFiltresDto> getAllApplications() {
		List<ApplicationWithPermissionsAndFiltresDto> applicationsDto = new ArrayList<ApplicationWithPermissionsAndFiltresDto>();
		for (Application application : entDAO.getAllApplications()) {
			ApplicationWithPermissionsAndFiltresDto applicationDto = modelMapper.map(application, ApplicationWithPermissionsAndFiltresDto.class);
			applicationsDto.add(applicationDto);
		}
		return applicationsDto;
	}

	@Override
	public List<ApplicationDto> getApplicationsMoreClickedByUser(User user) {
		//récupération des applications les plus utilisées
		List<Application> applicationsMoreClicked = entDAO.getApplicationsMoreClickedByUser(user.getUid());
		List<ApplicationDto> applicationsMoreClickedDto = new ArrayList<ApplicationDto>();
		for (Application application : applicationsMoreClicked) {
			ApplicationDto applicationMoreClickedDto = modelMapper.map(application, ApplicationDto.class);
			applicationsMoreClickedDto.add(applicationMoreClickedDto);
		}
		return applicationsMoreClickedDto;
	}

	@Override
	public List<ApplicationDto> getApplicationsFavoritesByUser(User user) {
		//récupération des applications favorites
		List<Application> applicationsFavorites = entDAO.getApplicationsFavoritesByUser(user.getUid());
		List<ApplicationDto> applicationsFavoritesDto = new ArrayList<ApplicationDto>();
		for (Application application : applicationsFavorites) {
			ApplicationDto applicationFavoriteDto = modelMapper.map(application, ApplicationDto.class);
			applicationsFavoritesDto.add(applicationFavoriteDto);
		}
		return applicationsFavoritesDto;
	}

	public ApplicationDto getApplicationByFname(String fname) {
		Application application = entDAO.getApplicationByFname(fname);
		ApplicationDto applicationDto = modelMapper.map(application, ApplicationDto.class);
		return applicationDto;
	}
	
	/**************************************/
	/* Gestion du menu 					  */
	/**************************************/
	@Override
	public MenusApplicationsDto getMenusAndApplicationMandatoryByUser(String uid) {

		Boolean tousEmployee, tousFaculty, tousResearch, tousAffiliate, tousRetired, tousEtu, tousAlum;
		tousEmployee = tousFaculty = tousResearch = tousAffiliate = tousRetired = tousEtu = tousAlum = false;
		
		// récupération des groupes LDAP de l'utilisateur
		LdapPerson ldapPerson = entDAO.getInfoUserLdap(uid);
		// récupération de tous les menus
		List<Menu> allMenus = entDAO.getAllMenus();
		// récupération des applications qui matchent avec les filtres group et user
		List<Application> applicationsAccessibles = entDAO.getAllApplicationsByGroupsLdapAndUid(ldapPerson.getMemberOf(), uid);
		
		// Menu de l'utilisateur
		List<MenuDto> menusUserDto = new ArrayList<MenuDto>();
		// Applications dans le menu de l'utilisateur
		MenusApplicationsDto menusApplicationsDto = new MenusApplicationsDto();
		// Liste des applications obligatoires
		List<ApplicationDto> applicationsMandatoryDto = new ArrayList<ApplicationDto>();
		// Liste des applications qui matchent eduPersonAffiliation
		List<Application> applicationsEdupersonAffiliation = new ArrayList<Application>();
		// Liste des applications autorisées pour l'utilisateur ???
		List<ApplicationDto> applicationsAccessiblesDto = new ArrayList<ApplicationDto>();
		
		// on ajoute, dans le menu, les applications qui matchent avec eduPersonAffiliation et les applications pour tout le monde
		for (String eduPersonAffiliation : ldapPerson.getEdupersonaffiliation()) {
			switch (eduPersonAffiliation) {
			case "employee":
				tousEmployee = true;
				break;
			case "faculty":
				tousFaculty = true;
				break;
			case "researcher":
				tousResearch = true;
				break;
			case "affiliate" :
				tousAffiliate = true;
				break;
			case "retired" :
				tousRetired = true;
				break;
			case "student" :
				tousEtu = true;
				break;
			case "alum" :
				tousAlum = true;
				break;
			default:
				break;
			}
		}
		
		// On range, dans applicationsAccessibles, les applications qui matchent avec eduPersonAffiliation et les applications pour tout le monde (applications actives)
		applicationsEdupersonAffiliation = entDAO.getAllApplicationsTousOrEduPersonAffiliation(tousEmployee, tousFaculty, tousResearch,	tousAffiliate, tousRetired, tousEtu, tousAlum);
		for (Application applicationEdupersonAffiliation : applicationsEdupersonAffiliation) {
			if(!applicationsAccessibles.contains(applicationEdupersonAffiliation)) {
				applicationsAccessibles.add(applicationEdupersonAffiliation);
			}			
		}
		
		// On filtre applicationsAccessible et on range toutes les applications actives dans applicationsAccessiblesDto
		for(Application application : applicationsAccessibles) {
			ApplicationDto applicationDto = modelMapper.map(application, ApplicationDto.class);
			if (isActive(application)) {
				applicationsAccessiblesDto.add(applicationDto);
			}
		}
		
		// Pour chaque menus/sous-menus, on ne conserve que les applications autorisées
		for (Menu menu : allMenus) {
			if(menu.getApplications().size() > 0) {
				MenuDto menuDto = modelMapper.map(menu, MenuDto.class);
				menuDto.getApplications().retainAll(applicationsAccessiblesDto);
				if(menuDto.getApplications().size() > 0) {
					menusUserDto.add(menuDto);
				}
			}
		}
		
		for (MenuDto menuDto : menusUserDto) {
			for(ApplicationDto applicationDto : menuDto.getApplications()) {
				applicationDto.setIsMandatory(false);
				chekConditionAffichage(applicationDto, ldapPerson);
				if(applicationDto.getIsMandatory() && applicationDto.getActif()) {
					if(!applicationsMandatoryDto.contains(applicationDto)) {
						applicationsMandatoryDto.add(applicationDto);
					}
				}
			}
		}

		menusApplicationsDto.setApplicationsMandatory(applicationsMandatoryDto);
		menusApplicationsDto.setMenu(menusUserDto);
		
		return menusApplicationsDto;
	}

	// Si l'application est active ET dans la période de temps définie
	public boolean isActive (Application application) {
		Date date= new Date();
		long time = date.getTime();
		Timestamp curentDate = new Timestamp(time);
		if (application.getActif()) {
			if (application.getDateDebut() != null && application.getDateDebut().after(curentDate)) {
				return false;
			}else if (application.getDateFin() != null && application.getDateFin().before(curentDate)) {
				return false;
			}
			return true;
		}else {
			return false;
		}
		
	}
	
	public void chekConditionAffichage(ApplicationDto application, LdapPerson ldapPerson) {	
		// on vérifie si affichage mandatory
		if (application.getConditionAffichage() != null && application.getConditionAffichage().length() > 0) {
			if (application.getConditionAffichage().toLowerCase().equals("mandatory")) {
				application.setIsMandatory(true);
			} else if (application.getConditionAffichage().toLowerCase().equals("mandatory-pers")
					&& !ldapPerson.getEduPersonPrimaryAffiliation().toString().toLowerCase().equals("student")
					&& !ldapPerson.getEduPersonPrimaryAffiliation().toString().toLowerCase().equals("alum")) {
				application.setIsMandatory(true);
			} else if (application.getConditionAffichage().toLowerCase().equals("mandatory-etu")
					&& (ldapPerson.getEduPersonPrimaryAffiliation().toString().toLowerCase().equals("student")
							|| ldapPerson.getEduPersonPrimaryAffiliation().toString().toLowerCase().equals("alum"))) {
				application.setIsMandatory(true);
			} else if (application.getConditionAffichage().toLowerCase().equals("mandatory-alum")
					&& (ldapPerson.getEduPersonPrimaryAffiliation().toString().toLowerCase().equals("alum"))) {
				application.setIsMandatory(true);
			}
		}
	}

	@Override
	public void addMenu(MenuJson menuJson) {

		List<Application> applications = new ArrayList<Application>();

		Menu menu = modelMapper.map(menuJson, Menu.class);

		for (Long applicationJsonId : menuJson.getApplicationsJsonId()) {
			Application application = entDAO.getApplicationById(applicationJsonId);
			applications.add(application);
		}
		menu.setApplications(applications);
		menu.setPosition(entDAO.getAllMenus().size()+1);
		entDAO.addMenu(menu);
	}

	@Override
	public int deleteMenu(MenuJson menuJson) {
		if (menuJson.getId() != null) {
			int cr =  entDAO.deleteMenu(menuJson.getId());
			for(Menu menu : entDAO.getAllMenus()){
				menu.setPosition(menu.getPosition() -1);
				entDAO.updateMenu(menu);
			}
		}
		logger.error("erreur execution du ws delete-application - id null: ");
		return -1;
	}

	@Override
	public void updateMenu(MenuJson menuJson) {
		List<Application> applications = new ArrayList<Application>();

		Menu menu = entDAO.getMenuById(menuJson.getId());
		menu.setName(menuJson.getName());
		menu.setPosition(menuJson.getPosition());
		menu.setApplications(null);
		for (Long idApplication : menuJson.getApplicationsJsonId()) {
			Application application = entDAO.getApplicationById(idApplication);
			applications.add(application);
		}
		menu.setApplications(applications);

		entDAO.updateMenu(menu);
	}

	@Override
	public void updateMenuPosition(MenuJson menuJson) {
//		List<Application> applications = new ArrayList<Application>();

		Menu menu = entDAO.getMenuById(menuJson.getId());
		menu.setPosition(menuJson.getPosition());

		entDAO.updateMenu(menu);
	}

	@Override
	public List<MenuDto> getAllMenus() {
		List<Menu> menus = entDAO.getAllMenus();
		List<MenuDto> menusDto = new ArrayList<MenuDto>();

		for (Menu menu : menus) {
			List<ApplicationDto> applicationsDto = new ArrayList<ApplicationDto>();
			MenuDto menuDto = modelMapper.map(menu, MenuDto.class);
			for (Application application : menu.getApplications()) {
				ApplicationDto applicationDto = modelMapper.map(application, ApplicationDto.class);
				applicationsDto.add(applicationDto);
			}
			menuDto.setApplications(applicationsDto);
			menusDto.add(menuDto);
		}
		return menusDto;
	}

	/*********************************************/
	/* Gestion des permissions des applications */
	/*********************************************/
	@Override
	public void addApplicationPermission(ApplicationPermissionJson applicationPermissionJson) {
		ApplicationPermission applicationPermission = modelMapper.map(applicationPermissionJson,
				ApplicationPermission.class);
		entDAO.addApplicationPermission(applicationPermission);
	}

	@Override
	public int deleteApplicationPermission(ApplicationPermissionJson applicationPermissionJson) {

		ApplicationPermission applicationPermission = entDAO
				.getApplicationPermissionById(applicationPermissionJson.getId());
		if (applicationPermission != null) {
			// récupératon des applications qui contiennent la permission + mise a jour
			if (applicationPermission.getTypeAccess().equals("GROUP_LDAP")) {
				List<Application> applications = entDAO.getAllApplicationsByGroupLdap(applicationPermission.getValue());
				for (Application application : applications) {
					application.getApplicationsPermission().remove(applicationPermission);
					entDAO.updateApplication(application);
				}
			}
			if (applicationPermission.getTypeAccess().equals("USER")) {
				List<Application> applications = entDAO.getAllApplicationsByUser(applicationPermission.getValue());
				for (Application application : applications) {
					application.getApplicationsPermission().remove(applicationPermission);
					entDAO.updateApplication(application);
				}
			}
			// suppression de la permission
			return entDAO.deleteApplicationPermission(applicationPermissionJson.getId());
		}
		return -1;
	}

	@Override
	public void updateApplicationPermission(ApplicationPermissionJson applicationPermissionJson) {
		ApplicationPermission applicationPermission = entDAO
				.getApplicationPermissionById(applicationPermissionJson.getId());
		applicationPermission.setValue(applicationPermissionJson.getValue());
		applicationPermission.setTypeAccess(applicationPermissionJson.getTypeAccess());
		entDAO.updateApplicationPermission(applicationPermission);
	}

	@Override
	public List<ApplicationPermissionDto> getAllLdapApplicationPermission() {
		List<ApplicationPermissionDto> applicationsPermissionDto = new ArrayList<ApplicationPermissionDto>();
		List<ApplicationPermission> applicationsPermission = entDAO.getAllLdapApplicationPermission();
		for (ApplicationPermission applicationPermission : applicationsPermission) {
			applicationsPermissionDto.add(modelMapper.map(applicationPermission, ApplicationPermissionDto.class));
		}
		return applicationsPermissionDto;
	}

	@Override
	public List<ApplicationPermissionDto> getAllUserApplicationPermission() {
		List<ApplicationPermissionDto> applicationsPermissionDto = new ArrayList<ApplicationPermissionDto>();

		List<ApplicationPermission> applicationsPermission = entDAO.getAllUserApplicationPermission();
		for (ApplicationPermission applicationPermission : applicationsPermission) {
			applicationsPermissionDto.add(modelMapper.map(applicationPermission, ApplicationPermissionDto.class));
		}
		return applicationsPermissionDto;
	}

	/**************************************/
	/* Gestion des favoris */
	/**************************************/
	@Override
	public List<Favoris> getFavorisByUser(User user) {
		return entDAO.getFavorisByUser(user.getUid());
	}

	@Override
	public void addFavoris(FavorisJson favorisJson) {
		Favoris favoris = modelMapper.map(favorisJson, Favoris.class);
		favoris.setDate(new Date());
		favoris = entDAO.addFavoris(favoris);
	}

	@Override
	public void deleteFavoris(FavorisJson favorisJson) {
		Favoris favorisToDelete = entDAO.getFavoris(favorisJson.getUid(), favorisJson.getApplicationId());
		entDAO.deleteFavoris(favorisToDelete.getId());
	}


	@Override
	public void addCompteurClick(CompteurClickJson compteurClickJson) {
		CompteurClick compteurClick = modelMapper.map(compteurClickJson, CompteurClick.class);
		try {
			CompteurClick compteurClickBase = entDAO.getCompteurClick(compteurClick.getUid(),compteurClickJson.getApplicationId());
			//cas ou le compteur existe dejà en base : on update le compteur 
			compteurClickBase.setCompteur(compteurClickBase.getCompteur()+1);
			compteurClickBase.setClickDate(new Date());
			entDAO.updateCompteurClick(compteurClickBase);
		} catch (Exception NoResultException) {
			//cas ou le compteur n'existe pas en base : on ajoute le nouveau compteur 
			compteurClick.setCompteur(1);
			compteurClick.setClickDate(new Date());
			compteurClick = entDAO.addCompteurClick(compteurClick);
		}
	}


	@Override
	public void deleteCompteurClick(CompteurClickJson compteurClickJson) {
		CompteurClick compteurClickToDelete = entDAO.getCompteurClick(compteurClickJson.getUid(),compteurClickJson.getApplicationId());
		entDAO.deleteCompteurClick(compteurClickToDelete.getId());
	}
	
}
