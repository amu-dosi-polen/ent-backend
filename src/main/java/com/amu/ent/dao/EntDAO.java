package com.amu.ent.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.amu.ent.auth.user.LdapGroup;
import com.amu.ent.auth.user.LdapPerson;
import com.amu.ent.entity.Application;
import com.amu.ent.entity.ApplicationPermission;
import com.amu.ent.entity.CompteurClick;
import com.amu.ent.entity.Favoris;
import com.amu.ent.entity.Menu;
import com.amu.ent.entity.User;

@Transactional
@Repository
public class EntDAO implements IEntDAO {

	@Value("${ldap.base.people}")
	private String ldapBasePeople;

	@Value("${ldap.base.groups}")
	private String ldapBaseGroups;

    	@Value("${ldap.group-objectclass}")
	private String ldapGroupObjectClass;

	@Autowired
	private LdapTemplate ldapTemplate;

	@PersistenceContext
	private EntityManager entityManager;

	private static final Logger logger = LoggerFactory.getLogger(EntDAO.class);

	/**************************************/
	/* Gestion des autorisations */
	/**************************************/
	@Override
	public Boolean isAuthorized(String uid) {
		Query query = entityManager.createQuery("SELECT count(1) FROM User WHERE uid=:uid").setParameter("uid", uid);

		logger.debug("isAuthorized : " + uid);
		if (query.getSingleResult().toString().equals("0")) {
			return false;
		}
		return true;
	}

	/**************************************/
	/* Données LDAP */
	/**************************************/
	@Override
	public LdapPerson getInfoUserLdap(String uid) {
		logger.debug("getInfoUserLdap : " + uid);
		String[] attrRetourLdap = { "mail", "sn", "givenName", "supannEntiteAffectationPrincipale", "memberOf", "eduPersonPrimaryAffiliation" , "edupersonaffiliation"};
		LdapPerson personne = new LdapPerson();
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", "eduPerson"));
		andFilter.and(new EqualsFilter("uid", uid));

		List<LdapPerson> searchLdap = ldapTemplate.search(ldapBasePeople, andFilter.encode(), 3, attrRetourLdap,
				new AttributesMapper<LdapPerson>() {
					public LdapPerson mapFromAttributes(Attributes attrs)
					throws NamingException, javax.naming.NamingException {
						if (attrs.get("mail") == null) {
							personne.setMail("");
						} else {
							personne.setMail(attrs.get("mail").get().toString());
						}
						personne.setName(attrs.get("sn").get().toString());
						personne.setFirstname(attrs.get("givenName").get().toString());
						personne.setEduPersonPrimaryAffiliation(attrs.get("eduPersonPrimaryAffiliation").get().toString());
						if(attrs.get("edupersonaffiliation") != null) {
							NamingEnumeration members = attrs.get("edupersonaffiliation").getAll();
							while (members.hasMoreElements()) {
								personne.addEdupersonaffiliation(members.nextElement().toString());
							}
						}
						if(attrs.get("memberOf") != null) {
							NamingEnumeration members = attrs.get("memberOf").getAll();
							while (members.hasMoreElements()) {
								personne.addMemberOf(members.nextElement().toString());
							}
						}
						return personne;
					}
				});
		if (searchLdap.size() == 1) {
			return searchLdap.get(0);
		}
		return null;

	}
	
	@Override
	public List<LdapPerson> getUsersLdap(String uid) {
		logger.debug("getUsersLdap : " + uid);
		String[] attrRetourLdap = { "uid", "mail", "sn", "givenName", "supannEntiteAffectationPrincipale", "memberOf", "eduPersonPrimaryAffiliation" };
		AndFilter andFilter = new AndFilter();

		LikeFilter likeFilter = new LikeFilter("uid", "*" + uid + "*");
		andFilter.and(new EqualsFilter("objectclass", "eduPerson"));
		andFilter.and(likeFilter);

		List<LdapPerson> searchLdap = ldapTemplate.search(ldapBasePeople, andFilter.encode(), 3, attrRetourLdap,
				new AttributesMapper<LdapPerson>() {
					public LdapPerson mapFromAttributes(Attributes attrs)
							throws NamingException, javax.naming.NamingException {
						LdapPerson personne = new LdapPerson();
						personne.setUid(attrs.get("uid").get().toString());
						personne.setMail(attrs.get("mail").get().toString());
						personne.setName(attrs.get("sn").get().toString());
						personne.setFirstname(attrs.get("givenName").get().toString());
						personne.setEduPersonPrimaryAffiliation(attrs.get("eduPersonPrimaryAffiliation").get().toString());
						if(attrs.get("memberOf") != null) {
						NamingEnumeration members = attrs.get("memberOf").getAll();
							while (members.hasMoreElements()) {
								personne.addMemberOf(members.nextElement().toString());
							}
						}
						return personne;
					}
				});
		return searchLdap;

	}

	@Override
	public List<LdapGroup> getAllGroups(String groupResearch) {
		logger.debug("getAllGroups ");

		String[] attrRetourLdap = { "cn" };

		LikeFilter likeFilter = new LikeFilter("cn", "*" + groupResearch + "*");
		AndFilter andFilter = new AndFilter();
		andFilter.and(new EqualsFilter("objectclass", ldapGroupObjectClass));
		andFilter.and(likeFilter);
		List<LdapGroup> groups = new ArrayList<LdapGroup>();

		List<LdapGroup> searchLdap = ldapTemplate.search(ldapBaseGroups, andFilter.encode(), 3, attrRetourLdap,
				new AttributesMapper<LdapGroup>() {
					public LdapGroup mapFromAttributes(Attributes attrs)
							throws NamingException, javax.naming.NamingException {
						LdapGroup group = new LdapGroup();
						group.setCn(attrs.get("cn").get().toString());
						groups.add(group);
						return group;
					}
				});
		if (searchLdap.size() > 0) {
			return searchLdap;
		}
		return null;
	}

	/**************************************/
	/* Gestion des users */
	/**************************************/
	@Override
	public void createUser(User user) {
		entityManager.persist(user);
		logger.debug("createUser : " + user);
	}

	@Override
	public List<User> getAllUsers() {
		logger.debug("getAllUsers : ");
		String hql = "FROM User as user ORDER BY user.name ASC";
		return (List<User>) entityManager.createQuery(hql).getResultList();
	}

	@Override
	public int deleteUser(User user) {
		logger.debug("deleteUser : " + user);
		Query query = entityManager.createQuery("DELETE FROM User as user WHERE user.id=:id").setParameter("id",
				user.getId());
		return query.executeUpdate();
	}

	@Override
	public void updateUser(User user) {
		logger.debug("updateUser : " + user);
		entityManager.merge(user);
	}

	@Override
	public User getUserById(long id) {
		logger.debug("getUserById : " + id);
		Query query = entityManager.createQuery("FROM User as user WHERE user.id=:id").setParameter("id", id);
		List<User> list = query.getResultList();
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public User getUserByUid(String uid) {
		logger.debug("getUserByUid : " + uid);
		Query query = entityManager.createQuery("FROM User as user WHERE user.uid=:uid").setParameter("uid", uid);
		List<User> list = query.getResultList();
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	/******************************/
	/* Gestion des applications */
	/******************************/
	@Override
	public void createApplication(Application application) {
		logger.debug("createApplication : " + application);
		entityManager.persist(application);
	}

	@Override
	public List<Application> getAllApplications() {
		logger.debug("getAllApplications ");
		String hql = "FROM Application as application ORDER BY application.name ASC";
		return (List<Application>) entityManager.createQuery(hql).getResultList();
	}

	@Override
	public void deleteApplication(Application application) {
		logger.debug("deleteApplication id : " + application.getId());
		entityManager.remove(application);
	}

	@Override
	public void updateApplication(Application application) {
		logger.debug("updateApplication : " + application);
		entityManager.merge(application);
	}

	@Override
	public List<Application> getAllApplicationsByUser(String uid) {
		logger.debug("getAllApplicationsByUser ");
		Date date= new Date();
		long time = date.getTime();
		Timestamp curentDate = new Timestamp(time);
		Query query = entityManager
				.createQuery("FROM Application as application "
						+ " JOIN application.applicationsPermission as applicationsPermission "
						+ " WHERE applicationsPermission.value=:value"
						+ " AND applicationPermission.typeAccess=:typeAccess AND application.actif is true"
						+ " AND (application.dateDebut <= :now OR application.dateDebut is NULL)"
						+ " AND (application.dateFin >= :now OR application.dateFin is NULL)")
				.setParameter("value", uid)
				.setParameter("typeAccess", "USER")
				.setParameter("now", curentDate);
		List<Application> list = query.getResultList();
		return list;
	}

	@Override
	public List<Application> getAllApplicationsByGroupLdap(String groupLdap) {
		logger.debug("getAllApplicationsByGroupLdap ");
		Date date= new Date();
		long time = date.getTime();
		Timestamp curentDate = new Timestamp(time);
		Query query = entityManager.createQuery("SELECT application FROM Application as application "
						+ " JOIN application.applicationsPermission as applicationPermission "
						+ " WHERE applicationPermission.value=:value "
						+ " AND application.actif is true"
						+ " AND applicationPermission.typeAccess=:typeAccess"
						+ " AND (application.dateDebut <= :now OR application.dateDebut is NULL)"
						+ " AND (application.dateFin >= :now OR application.dateFin is NULL)")
				.setParameter("value", groupLdap)
				.setParameter("typeAccess", "GROUP_LDAP")
				.setParameter("now", curentDate);
		List<Application> list = query.getResultList();
		return list;
	}

	@Override
	public List<Application> getApplicationsMoreClickedByUser(String uid) {
		logger.debug("getApplicationsMoreClickedByUser ");
		Date date= new Date();
		long time = date.getTime();
		Timestamp curentDate = new Timestamp(time);
		Query query = entityManager.createQuery("SELECT application FROM Application as application "
			+ " JOIN application.compteursClick as compteurClick "
		  	+ " WHERE compteurClick.uid=:uid AND application.actif is true"
		  	+ " AND (application.dateDebut <= :now OR application.dateDebut is NULL)"
			+ " AND (application.dateFin >= :now OR application.dateFin is NULL)"
		    + " ORDER BY compteurClick.compteur DESC")
		  .setParameter("uid", uid)
		  .setParameter("now", curentDate);
		query.setMaxResults(16);
		List<Application> list = query.getResultList();
		return list;
	}

	@Override
	public Application getApplicationById(Long id) {
		return entityManager.getReference(Application.class, id);
	}
	
	@Override
	public Application getApplicationByFname(String fname) {
		logger.debug("getApplicationByFname ");
		Query query = entityManager
				.createQuery("SELECT application FROM Application as application "
						+ " WHERE application.fname=:value")
				.setParameter("value", fname);
		Application application = (Application) query.getSingleResult();
		return application;
	}

	@Override
	public List<Application> getAllApplicationsByGroupsLdapAndUid(List<String> groupsLdap, String uid) {
		logger.debug("getAllMenusByGroupsLdapAndUid ");
		
		Date date= new Date();
		long time = date.getTime();
		Timestamp curentDate = new Timestamp(time);
		List<String> cnAttrs = new ArrayList<String>();
		if (groupsLdap != null) {
			for (String group : groupsLdap) {
				cnAttrs.add(group.toLowerCase().split("cn=")[1].split(",")[0]);		
			}
		}
		Query query = entityManager.createQuery("SELECT application FROM Application as application "
				+ " INNER JOIN application.applicationsPermission as applicationsPermission "
				+ " WHERE (applicationsPermission.value IN :groupsLdap AND applicationsPermission.typeAccess=:typeAccessLdap )"
				+ " OR "
				+ "(applicationsPermission.value =:value AND applicationsPermission.typeAccess=:typeAccess)"
				+ " AND application.actif is true"
				+ " AND (application.dateDebut <= :now OR application.dateDebut is NULL)"
				+ " AND (application.dateFin >= :now OR application.dateFin is NULL)"
				+ " GROUP BY application")
				.setParameter("now", curentDate)
				.setParameter("groupsLdap", cnAttrs)
				.setParameter("typeAccessLdap", "GROUP_LDAP")
				.setParameter("value", uid)
				.setParameter("typeAccess", "USER");
		List<Application> list =  query.getResultList();
		return list;
	}

	@Override
	public List<Application> getAllApplicationsTousOrEduPersonAffiliation(Boolean tousEmployee, Boolean tousFaculty, Boolean tousResearch, Boolean tousAffiliate, Boolean tousRetired, Boolean tousEtu, Boolean tousAlum) {
		logger.debug("getAllApplicationsTousOrEduPersonAffiliation ");
	
		Date date= new Date();
		long time = date.getTime();
		Timestamp curentDate = new Timestamp(time);
		
		StringBuffer filtreTous = new StringBuffer();
		if(tousEmployee) {
			filtreTous.append(" OR application.tousEmployee is true ");
		}
		if(tousFaculty) {
			filtreTous.append(" OR application.tousFaculty is true ");
		}
		if(tousResearch) {
			filtreTous.append(" OR application.tousResearch is true ");
		}
		if(tousAffiliate) {
			filtreTous.append(" OR application.tousAffiliate is true ");
		}
		if(tousRetired) {
			filtreTous.append(" OR application.tousRetired is true ");
		}
		if(tousEtu) {
			filtreTous.append(" OR application.tousEtu is true ");
		}
		if(tousAlum) {
			filtreTous.append(" OR application.tousAlum is true ");
		}
		Query query = entityManager.createQuery("SELECT application FROM Application as application "
				+ " WHERE "
				+ " ( application.tous is true"
				+ filtreTous
				+ ")"
				+ " AND application.actif is true"
				+ " AND (application.dateDebut <= :now OR application.dateDebut is NULL)"
				+ " AND (application.dateFin >= :now OR application.dateFin is NULL)"
				+ " GROUP BY application")
				.setParameter("now", curentDate);
		List<Application> list = query.getResultList();
		return list;

	}

	@Override
	public List<Application> getApplicationsFavoritesByUser(String uid) {
		logger.debug("getApplicationsFavoritesByUser ");
		Date date= new Date();
		long time = date.getTime();
		Timestamp curentDate = new Timestamp(time);
		Query query = entityManager.createQuery("SELECT application FROM Application as application "
				+ " INNER JOIN application.favorites as favoris "
				+ " WHERE favoris.uid=:uid "
				+ " AND application.actif is true"
				+ " AND (application.dateDebut <= :now OR application.dateDebut is NULL)"
				+ " AND (application.dateFin >= :now OR application.dateFin is NULL)"
				+ " ORDER BY favoris.position")
				.setParameter("uid", uid)
				.setParameter("now", curentDate);
		List<Application> list = query.getResultList();
		return list;
	}

	/******************************/
	/* Gestion des menus */
	/******************************/

	@Override
	public void addMenu(Menu menu) {
		logger.debug("addMenu : " + menu);
		entityManager.persist(menu);
	}

	@Override
	public int deleteMenu(Long id) {
		logger.debug("deleteMenu id : " + id);
		Query query = entityManager.createQuery("DELETE FROM Menu as menu WHERE menu.id=:id")
				.setParameter("id", id);
		return query.executeUpdate();
	}

	@Override
	public void updateMenu(Menu menu) {
		entityManager.merge(menu);
	}

	@Override
	public List<Menu> getAllMenusByApplication(Long idApplication) {
		logger.debug("getAllMenusByApplication : idApplication: " + idApplication);
		Date date= new Date();
		long time = date.getTime();
		Timestamp curentDate = new Timestamp(time);
		Query query = entityManager.createQuery("SELECT menu FROM Menu as menu "
				+ " INNER JOIN menu.applications as applications"
				+ " WHERE applications.id=:id")
				.setParameter("id", idApplication);
		List<Menu> list = query.getResultList();
		return list;
	}

	@Override
	public Menu getMenuById(Long id) {
		return entityManager.getReference(Menu.class, id);
	}
	
	@Override
	public List<Menu> getAllMenus() {
		logger.debug("getAllMenus ");
		Query query = entityManager.createQuery("SELECT menu FROM Menu as menu "
												+ " ORDER BY menu.position");
		List<Menu> list = query.getResultList();
		return list;
	}
	

	/******************************/
	/* Gestion des favoris */
	/******************************/
	@Override
	public Favoris getFavoris(String uid, Long appId) {
		logger.debug("getFavorisByUserAndAppliId ");
		Query query = entityManager.createQuery("SELECT favoris FROM Favoris as favoris"
				+ " WHERE favoris.uid=:uid"
				+ " AND application_id =:appId")
				.setParameter("uid", uid)
				.setParameter("appId", appId);
		return (Favoris) query.getSingleResult();
	}
	
	@Override
	public List<Favoris> getFavorisByUser(String uid) {
		logger.debug("getFavorisByUser ");
		Query query = entityManager.createQuery("SELECT favoris FROM Favoris as favoris "
				+ " WHERE favoris.uid=:uid")
				.setParameter("uid", uid);
		List<Favoris> list = query.getResultList();
		return list;
	}

	@Override
	public Favoris getFavorisById(Long id) {
		logger.debug("getFavorisById  id: " + id);
		return entityManager.getReference(Favoris.class, id);
	}

	@Override
	public Favoris addFavoris(Favoris favoris) {
		logger.debug("addFavoris : " + favoris);
		entityManager.persist(favoris);
		return favoris;
	}
	
	@Override
	public int deleteFavoris(Long id) {
		logger.debug("deleteFavoris id : " + id);
		Query query = entityManager.createQuery("DELETE FROM Favoris WHERE id = :id").setParameter("id", id);
		return query.executeUpdate();
	}

	@Override
	public void updateFavoris(Favoris favoris) {
		entityManager.merge(favoris);

	}

	
	/****************************************/
	/* Gestion des compteurs de clicks */
	/****************************************/

	@Override
	public CompteurClick getCompteurClick(String uid, Long appId) throws NoResultException {
		Query query = entityManager.createQuery("SELECT compteurClick FROM CompteurClick as compteurClick"
				+ " WHERE compteurClick.uid=:uid"
				+ " AND application_id=:appId")
				.setParameter("uid", uid)
				.setParameter("appId", appId);
		return (CompteurClick) query.getSingleResult();
	}
	
	@Override
	public CompteurClick addCompteurClick(CompteurClick compteurClick) {
		logger.debug("addCompteurClick : " + compteurClick);
		entityManager.persist(compteurClick);
		return compteurClick;
	}

	@Override
	public int deleteCompteurClick(Long id) {
		logger.debug("deleteCompteurClick id : " + id);
		Query query = entityManager.createQuery("DELETE FROM CompteurClick as CompteurClick WHERE CompteurClick.id=:id").setParameter("id", id);
		return query.executeUpdate();
	}

	@Override
	public void updateCompteurClick(CompteurClick compteurClick) {
		entityManager.merge(compteurClick);

	}


	/*********************************************/
	/* Gestion des permissions des applications	 */
	/*********************************************/
	@Override
	public ApplicationPermission getApplicationPermissionById(Long id) {
		logger.debug("ApplicationPermission : " + id);
		Query query = entityManager.createQuery("FROM ApplicationPermission as applicationPermission WHERE applicationPermission.id=:id").setParameter("id", id);
		List<ApplicationPermission> list = query.getResultList();
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public void addApplicationPermission(ApplicationPermission applicationPermission) {
		entityManager.persist(applicationPermission);
		logger.debug("createApplicationPermission : " + applicationPermission);		
	}

	@Override
	public int deleteApplicationPermission(Long id) {
		logger.debug("ApplicationPermission id : " + id);
		Query query = entityManager.createQuery("DELETE FROM ApplicationPermission as applicationPermission WHERE applicationPermission.id=:id").setParameter("id", id);
		return query.executeUpdate();
	}

	@Override
	public void updateApplicationPermission(ApplicationPermission applicationPermission) {
		logger.debug("ApplicationPermission : ");
		entityManager.merge(applicationPermission);		
	}

	@Override
	public List<ApplicationPermission> getAllLdapApplicationPermission() {
		logger.debug("getAllUsers : ");
		Query query = entityManager.createQuery("FROM ApplicationPermission as applicationPermission WHERE applicationPermission.typeAccess = 'GROUP_LDAP' ORDER BY applicationPermission.value ASC");
		List<ApplicationPermission> list = query.getResultList();
		return list;
	}

	@Override
	public List<ApplicationPermission> getAllUserApplicationPermission() {
		logger.debug("getAllUsers : ");
		Query query = entityManager.createQuery("FROM ApplicationPermission as applicationPermission WHERE applicationPermission.typeAccess = 'USER' ORDER BY applicationPermission.value ASC");
		List<ApplicationPermission> list = query.getResultList();
		return list;
	}

}
