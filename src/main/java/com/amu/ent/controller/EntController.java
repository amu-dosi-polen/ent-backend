package com.amu.ent.controller;

import static com.amu.ent.auth.security.SecurityConstants.HEADER_STRING;
import static com.amu.ent.auth.security.SecurityConstants.TOKEN_PREFIX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.util.UriComponentsBuilder;
import com.amu.ent.auth.user.LdapGroup;
import com.amu.ent.auth.user.LdapPerson;
import com.amu.ent.dto.ApplicationDto;
import com.amu.ent.dto.ApplicationWithPermissionsAndFiltresDto;
import com.amu.ent.dto.MenuDto;
import com.amu.ent.dto.MenusApplicationsDto;
import com.amu.ent.dto.UserDto;
import com.amu.ent.entity.User;
import com.amu.ent.json.ApplicationJson;
import com.amu.ent.json.CompteurClickJson;
import com.amu.ent.json.FavorisJson;
import com.amu.ent.json.FnameUidJson;
import com.amu.ent.json.MenuJson;
import com.amu.ent.json.UserJson;
import com.amu.ent.service.IEntService;
import com.amu.ent.utils.UrlGenerator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import com.amu.ent.configuration.RsaJWT;
import java.security.interfaces.RSAPrivateKey;

@RestController
@RequestMapping("/ent")
@CrossOrigin(origins="http://localhost:4200")
public class EntController {

	@Autowired
	private IEntService entService;
	

	@Autowired
	private UrlGenerator urlGenerator;

	private static final Logger logger = LoggerFactory.getLogger(EntController.class);

	@Value("${config.url.siamu}") private String urlSiamu;
	@Value("${config.jwt.ttl}") private String jwtTTL;
	@Value("#{'${config.admin.group}'.split(';')}") private List<String> adminGroup;
	@Value("${config.context.path}") private String contextPath;
	@Value("${cas.cas-server-url-prefix}") private String logoutURL;
	
	
	
	private Claims getClaims(String token) {
		
		Claims claims=null;
		RSAPublicKey publickey = RsaJWT.readPublicKey();
		try {
			claims = Jwts.parser()
	           .setSigningKey(publickey)
	           .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
	           .getBody();
		} catch (ExpiredJwtException | SignatureException e) {
				return null;
		}
		return claims;
	}
	
	/************************************************************************/
	/* WS ANGULAR */
	/************************************************************************/
	@GetMapping("login")
	public ResponseEntity<String> login(@Context HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String role = "normal";
		String then = request.getParameter("then");
		Long jwtLongTTL= Long.parseLong(jwtTTL);
		if (then != null) {
			String url = urlGenerator.goTo(request, "/");
			response.sendRedirect(url);
		}

		String content, type;
		if (request.getParameter("postMessage") != null) {
			type = "text/html";
			content = "Login success, please wait...\n<script>\n (window.opener ? (window.opener.postMessage ? window.opener : window.opener.document) : window.parent).postMessage('loggedUser=', '*');\n</script>";
		} else if (request.getParameter("callback") != null) {
			type = "application/x-javascript";
			content = request.getParameter("callback");
		} else {
			type = "application/json";
			content = "()";
		}

		MultiValueMap<String, String> headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/x-javascript");
		headers.add("Cache-Control", "no-cache");
		String uid=request.getUserPrincipal().toString();
		User user = entService.getUserByUid(uid);


		LdapPerson ldapPerson = entService.getInfoUserLdap(uid);
		String mail=ldapPerson.getMail();
		String name=ldapPerson.getName();
		String firstname=ldapPerson.getFirstname();
		if (user != null && user.getRole().equals("ADMIN")) {
			role = "ADMIN";
		} else {
			List<String> groups = ldapPerson.getMemberOf();
			if (groups!=null) {
				int i = 0;
				while (i < groups.size()) {
					if (adminGroup.contains(groups.get(i))) {
						role="ADMIN";
					}
					i++;
				}
			}
		}

		RSAPrivateKey privatekey = RsaJWT.readPrivateKey();
		String token = Jwts.builder().setSubject(uid)
				.setExpiration(new Date(System.currentTimeMillis() + jwtLongTTL))
				.claim("mail", mail)
				.claim("name",name)
				.claim("firstname",firstname)
				.claim("role",role)
				.signWith(SignatureAlgorithm.RS256, privatekey).compact();

		content += ("({ \"token\" : \"" + token + "\" })");
		headers.add(HEADER_STRING, TOKEN_PREFIX + token);
		// Fume le cookie ajouté par Tomcat
		headers.add("Set-Cookie","JSESSIONID=; Max-Age=0;Path=" +  contextPath);

		ResponseEntity<String> responseEntity = new ResponseEntity<>(content, headers, HttpStatus.OK);

		return responseEntity;
	}

	@GetMapping("logout")
	public ResponseEntity<String> logout(@Context HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		MultiValueMap<String, String> headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "text/plain");
		headers.add("Cache-Control", "no-cache");
		headers.add("Set-Cookie","JSESSIONID=; Max-Age=0;Path=" +  contextPath);
		headers.add("Location",logoutURL +  "/logout");
		headers.add("Access-Control-Expose-Headers", "Location");
		ResponseEntity<String> responseEntity = new ResponseEntity<>(headers,HttpStatus.OK);

		return responseEntity;

	
	}
	/**************************************/
	/* Gestion des requetes LDAP */
	/**************************************/
	@PostMapping("get-all-groups")
	public ResponseEntity<String> getAllGroups(@Context HttpServletRequest request, @RequestBody String groupResearch,
			UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				List<LdapGroup> groups = entService.getAllGroups(groupResearch);
				if (groups != null) {
					logger.debug("execution du get-all-groups par l uid : " + uid);
					return new ResponseEntity(groups, HttpStatus.OK);
				}
			}
		}
		logger.info("uid inconnu ou non autorisé pour le get-all-groups : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	@PostMapping("get-users-ldap")
	public ResponseEntity<String> getUsersLdap(@Context HttpServletRequest request, @RequestBody String userLdap,
			UriComponentsBuilder builder) {

		User user = new User();
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();

		if (uid != null) {
			user = entService.getUserByUid(uid);
			if (user != null && user.getRole().equals("ADMIN")) {

				List<LdapPerson> ldapPerson = entService.getUsersLdap(userLdap);

				if (ldapPerson != null) {
					logger.debug("execution du get-users-ldap par l uid : " + uid);
					return new ResponseEntity(ldapPerson, HttpStatus.OK);
				}
			}
		}
		logger.info("uid inconnu ou non autorisé pour le get-users-ldap : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	/**************************************/
	/* Gestion des users */
	/**************************************/
	@PostMapping("add-user")
	public ResponseEntity addUser(@Context HttpServletRequest request, @RequestBody UserJson userJson,
			UriComponentsBuilder builder) {

		User user = new User();
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {

				LdapPerson personne = entService.getInfoUserLdap(userJson.getUid());

				if (personne != null) {
					user = new User();
					user.setName(personne.getName());
					user.setFirstname(personne.getFirstname());
					user.setRole(userJson.getRole());
					user.setUid(userJson.getUid());

					entService.createUser(user);

					logger.debug("execution du ws add-user par l uid : " + uid);
					return new ResponseEntity(HttpStatus.OK);
				}
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws add-user : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	@PostMapping("delete-user")
	public ResponseEntity deleteUser(@Context HttpServletRequest request, @RequestBody UserJson userJson,
			UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				User user = new User();
				user.setId(userJson.getId());
				int cr = entService.deleteUser(user);

				if (cr > 0) {
					logger.debug("execution du ws delete-user par l uid : " + uid);
					return new ResponseEntity(HttpStatus.OK);
				} else {
					logger.info("user à supprimer non trouvé : id = " + userJson.getId());
					return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws delete-user : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	@PostMapping("update-user")
	public ResponseEntity updateUser(@Context HttpServletRequest request, @RequestBody UserJson userJson,
			UriComponentsBuilder builder) {

		User user = new User();
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {

				user = entService.getUserById(userJson.getId());
				user = new User();
				user.setName(userJson.getName());
				user.setFirstname(userJson.getFirstname());
				user.setRole(userJson.getRole());
				user.setUid(uid);

				entService.updateUser(user);

				logger.debug("execution du ws update-user par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws update-user : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);

	}

	@GetMapping("info-user")
	public ResponseEntity<User> getInfoUser(@Context HttpServletRequest request) {
		// Cherche dans la table User les admin avec un id
		User user = new User();
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		if (uid != null) {
			user = entService.getUserByUid(uid);
			if (user != null) {
				logger.debug("execution du ws info-user par l uid : " + uid);
				return new ResponseEntity<User>(user, HttpStatus.OK);
			}
		}
		return new ResponseEntity<User>(user, HttpStatus.FORBIDDEN);
	}

	// Admin / Gestionnaires
	@GetMapping("all-users")
	public ResponseEntity<List<UserDto>> getAllUsers(@Context HttpServletRequest request) {

		List<UserDto> users = new ArrayList<UserDto>();
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				users = entService.getAllUsers();
				if (users.size() != 0) {
					logger.debug("execution du ws info-user par l uid : " + uid);
					return new ResponseEntity<List<UserDto>>(users, HttpStatus.OK);
				}
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws update-user : " + uid);
		return new ResponseEntity<List<UserDto>>(users, HttpStatus.FORBIDDEN);

	}

	/**************************************/
	/* Gestion des applications */
	/**************************************/
	@PostMapping("add-application")
	public ResponseEntity addApplication(@Context HttpServletRequest request,
			@RequestBody ApplicationJson applicationJson, UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				entService.createApplication(applicationJson);
				logger.debug("execution du ws add-application par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws add-application : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	@PostMapping("delete-application")
	public ResponseEntity deleteApplication(@Context HttpServletRequest request,
			@RequestBody ApplicationJson applicationJson, UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				entService.deleteApplication(applicationJson);
				logger.debug("execution du ws delete-application par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws delete-application : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	@PostMapping("update-application")
	public ResponseEntity updateApplication(@Context HttpServletRequest request,
			@RequestBody ApplicationJson applicationJson, UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				entService.updateApplication(applicationJson);
				logger.debug("execution du ws update-application par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws update-application : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	@GetMapping("get-all-applications")
	public ResponseEntity<ApplicationWithPermissionsAndFiltresDto> getAllApplications(
			@Context HttpServletRequest request, UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }

		if (uid != null) {
			if (role.equals("ADMIN") || role.equals("DOSI")) { 
				List<ApplicationWithPermissionsAndFiltresDto> applicationsDto = entService.getAllApplications();
				logger.debug("execution du ws get-all-applications par l uid : " + uid);
				return new ResponseEntity(applicationsDto, HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws get-all-applications : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	/**************************************/
	/* Gestion des menus */
	/**************************************/
	@PostMapping("add-menu")
	public ResponseEntity addMenu(@Context HttpServletRequest request, @RequestBody MenuJson menuJson,
			UriComponentsBuilder builder) {

		User user = new User();
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				entService.addMenu(menuJson);
				logger.debug("execution du ws add-menu par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws add-menu : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	@GetMapping("get-all-menus")
	public ResponseEntity<MenuDto> getAllMenus(@Context HttpServletRequest request, UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN") || role.equals("DOSI")) {
				List<MenuDto> menus = entService.getAllMenus();
				logger.debug("execution du ws add-menu par l uid : " + uid);
				return new ResponseEntity(menus, HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws add-menu : " + uid);
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}

	@PostMapping("get-compteur")
	public ResponseEntity<String> getCompteur(@Context HttpServletRequest request, @RequestBody FnameUidJson fnameUserJson,
			UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
	    String mail;
	    if (claims.get("mail") !=null) {
	    	mail=claims.get("mail").toString();
	    }
	    else {
	    	return new ResponseEntity(HttpStatus.BAD_REQUEST);
	    }
		String fname = fnameUserJson.getFname();
		if (fname != null) {
			String uidRequest = fnameUserJson.getUid();
			if (uidRequest != null) {
				if(uid.equals(uidRequest)) {
				    URL url;
					try {
						StringBuilder param = new StringBuilder();
						ApplicationDto applicationDto = entService.getApplicationByFname(fname);

						if(!applicationDto.getRequete().isEmpty()) {
							param.append("?uid=" + uid + "&mailUser=" + mail);
							url = new URL(applicationDto.getRequete() + param);
						    HttpURLConnection con = (HttpURLConnection) url.openConnection();
						    con.setConnectTimeout(5000);
						    con.setReadTimeout(5000);
						    con.setRequestMethod("GET");
							if(!applicationDto.getKey().isEmpty()) {
								con.setRequestProperty("Ent-Key", applicationDto.getKey());
							}
						    BufferedReader in = new BufferedReader(
						    		  new InputStreamReader(con.getInputStream()));
						    		String inputLine;
						    		StringBuffer content = new StringBuffer();
						    		while ((inputLine = in.readLine()) != null) {
						    		    content.append(inputLine);
						    		}
						    		in.close();
						    con.disconnect();
						    return new ResponseEntity(content, HttpStatus.OK);
						}
					} catch (MalformedURLException e) {
						logger.debug("uid : " + uid + "mail : " +mail);
						logger.debug("MalformedURLException : " + e);

					} catch (IOException e) {
						logger.debug("uid : " + uid + "mail : " +mail);
						logger.debug("IOException : " + e);
					} catch (Exception e) {
						logger.debug("Exception : " + e);
					}
					return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
				}
			}
		}
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}
	
	@GetMapping("get-siamu")
	public ResponseEntity<String> getSiamu(@Context HttpServletRequest request, UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();

		if (uid != null) {
		    URL url;
			try {
				StringBuilder param = new StringBuilder();

				if(!urlSiamu.isEmpty()) {
					url = new URL(urlSiamu);
				    HttpURLConnection con = (HttpURLConnection) url.openConnection();
				    con.setConnectTimeout(5000);
				    con.setReadTimeout(5000);
				    con.setRequestMethod("GET");
					
				    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				    String inputLine;
				    StringBuffer content = new StringBuffer();
				    while ((inputLine = in.readLine()) != null) {
				        content.append(inputLine);
				    }
				    in.close();
				    con.disconnect();
				    return new ResponseEntity(content, HttpStatus.OK);
				}
			} catch (MalformedURLException e) {
				logger.debug("getSiamu : uid : " + uid );
				logger.debug("MalformedURLException : " + e);

			} catch (IOException e) {
				logger.debug("getSiamu : uid : " + uid);
				logger.debug("IOException : " + e);
			} catch (Exception e) {
				logger.debug("Exception : " + e);
			}
			return new ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE);
		}
		return new ResponseEntity(HttpStatus.FORBIDDEN);
	}
	
	@GetMapping("get-menus-applications-user")
	public ResponseEntity<MenuDto> getMenuUser(@Context HttpServletRequest request, UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		User user = new User();
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			user = entService.getUserByUid(uid);
			
			if (user == null && role == "normal" ) { // Pas admin, on lit le token
			    	user = new User();
			    	user.setName(claims.get("name").toString());
			    	user.setFirstname(claims.get("firstname").toString());
			    	user.setUid(uid);
			} else { //DOSI ou ADMIN
				//cas d'un changement d'identité Admin
				if (request.getParameter("changeIdentity") != null) {
					uid = request.getParameter("changeIdentity");
					LdapPerson personne = entService.getInfoUserLdap(uid);
					if (personne != null) {
						user = new User();
						user.setName(personne.getName());
						user.setFirstname(personne.getFirstname());
						user.setUid(uid);
					}
					else {
						logger.info("uid inconnu dans la base ldap: " + uid);
						return new ResponseEntity(HttpStatus.BAD_REQUEST);
					}
				}
				else { //autre role et sans chgt identité
					user = new User();
			    	user.setName(claims.get("name").toString());
			    	user.setFirstname(claims.get("firstname").toString());
			    	user.setUid(uid);
				}
			}
			
			MenusApplicationsDto menusApplicationsDto = entService.getMenusAndApplicationMandatoryByUser(uid);
			List <ApplicationDto> applicationsFavorites = entService.getApplicationsFavoritesByUser(user);
			List <ApplicationDto> applicationsMoreClickedByUser = entService.getApplicationsMoreClickedByUser(user);
			// Liste des applications non autorisées
			List <ApplicationDto> favToRemove = new ArrayList<ApplicationDto>();
			List <ApplicationDto> mostClickedToRemove = new ArrayList<ApplicationDto>();

			//en premier lieu, on cherche les applis non autorisées dans "mes favoris" et "mes plus fréquentés"
			// On se base sur les applis du menu
			List<MenuDto> menusdto = menusApplicationsDto.getMenu();
			// Pour chaque appli "Mes Favoris"
			for (ApplicationDto appFav : applicationsFavorites) {
				boolean temFav = false;
				// Pour chaque menu/sous-menu
				for (MenuDto menuDto : menusdto) {
					// pour chaque appli du sous menu
					for (ApplicationDto app : menuDto.getApplications()) {
						if (appFav.getFname().equals(app.getFname())) {
							temFav = true;
							break;
						}
						if (temFav) break;
					}
				}
				// si aucun témoin à true, alors l'appli n'a rien à faire ici
				if (!temFav) {
					favToRemove.add(appFav);
				}
			}

			// rebelotte pour les applis "Mes plus fréquentés"
			for (ApplicationDto mostClickedApp : applicationsMoreClickedByUser) {
				boolean temMostClicked = false;
				for (MenuDto menuDto : menusdto) {
					for (ApplicationDto app : menuDto.getApplications()) {
						if (mostClickedApp.getFname().equals(app.getFname())) {
							temMostClicked = true;
							break;
						}
						if (temMostClicked) break;
					}
				}
				if(!temMostClicked) {
					mostClickedToRemove.add(mostClickedApp);
				}
			}

			//on nettoie les listes d'appli "Mes favoris" et "Mes plus fréquentés"
			applicationsFavorites.removeAll(favToRemove);
			applicationsMoreClickedByUser.removeAll(mostClickedToRemove);

			menusApplicationsDto.setApplicationsFavorites(applicationsFavorites);

			//pour pouvoir faire le compare lors du removeAll : IsMandatory est transient
			for (ApplicationDto applicationMandatory : menusApplicationsDto.getApplicationsMandatory()) {
				applicationMandatory.setIsMandatory(null);
			}
			if (!menusApplicationsDto.getApplicationsMandatory().isEmpty() && !applicationsMoreClickedByUser.isEmpty()) {
				// suppression des applications obligatoires
				applicationsMoreClickedByUser.removeAll(menusApplicationsDto.getApplicationsMandatory());
			}
			if (!menusApplicationsDto.getApplicationsFavorites().isEmpty() && !applicationsMoreClickedByUser.isEmpty()) {
				// suppression des applications favorites
				applicationsMoreClickedByUser.removeAll(menusApplicationsDto.getApplicationsFavorites());
			}
			//on repositionne l'info de isMandatory
			for (ApplicationDto applicationMandatory : menusApplicationsDto.getApplicationsMandatory()) {
				applicationMandatory.setIsMandatory(true);
			}

			menusApplicationsDto.setApplicationsMoreClicked(applicationsMoreClickedByUser);
			menusApplicationsDto.setName(user.getName());
			menusApplicationsDto.setFirstname(user.getFirstname());
			menusApplicationsDto.setEmail(user.getEmail());
			menusApplicationsDto.setUid((user).getUid());

			logger.debug("execution du ws get-menus-applications-user par l uid : " + uid);
			return new ResponseEntity(menusApplicationsDto, HttpStatus.OK);
		}
		logger.info("uid inconnu ou non autorisé pour le ws get-menus-applications-user : " + uid);
		return new ResponseEntity(HttpStatus.BAD_REQUEST);
	}

	@PostMapping("delete-menu")
	public ResponseEntity deleteMenu(@Context HttpServletRequest request, @RequestBody MenuJson menuJson,
			UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				int cr = entService.deleteMenu(menuJson);
				if (cr == -1) {
					return new ResponseEntity(cr, HttpStatus.NOT_ACCEPTABLE);
				}
				logger.debug("execution du ws delete-menu par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws delete-menu : " + uid);
		return new ResponseEntity(HttpStatus.BAD_REQUEST);
	}

	@PostMapping("update-menu")
	public ResponseEntity updateMenu(@Context HttpServletRequest request, @RequestBody MenuJson menuJson,
			UriComponentsBuilder builder) {

		User user = new User();
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				entService.updateMenu(menuJson);
				logger.debug("execution du ws update-menu par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws update-menu : " + uid);
		return new ResponseEntity(HttpStatus.BAD_REQUEST);
	}

	@PostMapping("update-menu-position")
	public ResponseEntity updateMenuPosition(@Context HttpServletRequest request, @RequestBody MenuJson menuJson,
			UriComponentsBuilder builder) {

		User user = new User();
		String uid = SecurityContextHolder.getContext().getAuthentication().getName();
		String token = request.getHeader(HEADER_STRING);
		String role = "normal";
		Claims claims=getClaims(token);
		if (claims == null)
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		if (claims.get("role") !=null) {
	    	role=claims.get("role").toString();
	    }
		if (uid != null) {
			if (role.equals("ADMIN")) {
				entService.updateMenuPosition(menuJson);
				logger.debug("execution du ws update-menu_position par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
			}
		}
		logger.info("uid inconnu ou non autorisé pour le ws update-menu_position : " + uid);
		return new ResponseEntity(HttpStatus.BAD_REQUEST);
	}

	/**************************************/

	/* Gestion des favoris */
	/**************************************/
	@PostMapping("add-favoris")
	public ResponseEntity addFavoris(@Context HttpServletRequest request, @RequestBody FavorisJson favorisJson,
			UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();

		if (uid != null) {
				favorisJson.setUid(uid);
				entService.addFavoris(favorisJson);
				logger.debug("execution du ws add-favoris par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
		}
		else {
				logger.info("uid inconnu dans la base ldap pour le ws add-favoris :" + uid);
				return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("delete-favoris")
	public ResponseEntity deleteFavoris(@Context HttpServletRequest request, @RequestBody FavorisJson favorisJson,
			UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();

		if (uid != null) {
				favorisJson.setUid(uid);
				entService.deleteFavoris(favorisJson);

				logger.debug("execution du ws delete-favoris par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
		}
		else {
				logger.info("uid inconnu dans la base ldap pour le ws delete-favoris :" + uid);
				return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("delete-compteur-click")
	public ResponseEntity deleteCompteurClick(@Context HttpServletRequest request, @RequestBody CompteurClickJson compteurClickJson,
			UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();

		if (uid != null) {
				compteurClickJson.setUid(uid);
				entService.deleteCompteurClick(compteurClickJson);

				logger.debug("execution du ws delete-compteur-click par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
		}
		else {
				logger.info("uid inconnu dans la base ldap pour le ws delete-compteur-click :" + uid);
				return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
	}

	/**************************************/
	/* Gestion des compteurs de clicks */
	/**************************************/
	@PostMapping("add-compteur-click")
	public ResponseEntity addCompteurClick(@Context HttpServletRequest request,
			@RequestBody CompteurClickJson compteurClickJson, UriComponentsBuilder builder) {

		String uid = SecurityContextHolder.getContext().getAuthentication().getName();

		if (uid != null) {
				compteurClickJson.setUid(uid);
				entService.addCompteurClick(compteurClickJson);
				logger.debug("execution du ws add-compteur-click par l uid : " + uid);
				return new ResponseEntity(HttpStatus.OK);
		}
		logger.info("uid inconnu ou non autorisé pour le ws add-compteur-click : " + uid);
		return new ResponseEntity(HttpStatus.BAD_REQUEST);
	}

}
