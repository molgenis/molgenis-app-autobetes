package org.molgenis.autobetes.controller;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE_TIME;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.autobetes.controller.AnonymousController.URI;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.net.URI;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.tree.AbstractEntity;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.autobetes.autobetes.ActivityEventInstance;
import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.autobetes.autobetes.EventInstance;
import org.molgenis.autobetes.autobetes.FoodEventInstance;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.rest.AttributeMetaDataResponse;
import org.molgenis.data.rest.EntityCollectionRequest;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.token.MolgenisToken;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.collect.Lists;

/**
 * Controller that handles anonymous requests
 */
@Controller
@RequestMapping(URI)
public class AnonymousController extends MolgenisPluginController
{
	public static final String ID = "anonymous";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String BASE_URI = "";

	@Autowired
	private DataService dataService;

	@Autowired
	private JavaMailSender mailSender;

	public AnonymousController()
	{
		super(URI);
	}

	@RequestMapping
	public String init()
	{
		return "view-home";
	}

	@RequestMapping(value = "/activate/{activationCode}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> activateUser(@PathVariable String activationCode)
	{
		System.out.println(">> Activation code: " + activationCode);

		MolgenisUser mu = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.ACTIVATIONCODE, activationCode), MolgenisUser.class);

		if (null == mu)
		{
			return response(false, "Registration failed. No user with this activation code.");
		}

		mu.setActive(true);

		dataService.update(MolgenisUser.ENTITY_NAME, mu);

		return response(true, "You're account is now active!");
	}

	@RequestMapping(value = "/registerUser", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> registerUser(@RequestBody RegistrationRequest registrationRequest,
			HttpServletRequest servletRequest)
	{
		// validate email + pw
		if (StringUtils.isBlank(registrationRequest.getEmail())
				|| StringUtils.isBlank(registrationRequest.getPassword()))
		{
			return response(false, "Registration failed. Please provide a valid email and password!");
		}
		
		MolgenisUser existingUser = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.EMAIL, registrationRequest.getEmail()), MolgenisUser.class);

		
		if (null != existingUser)
		{
			return response(false,
					"Registration failed. Email already exists. Please click 'Forgotten' to get a new password.");
		}
		MolgenisUser mu = new MolgenisUser();
		mu.setUsername(registrationRequest.getEmail());
		mu.setPassword(registrationRequest.getPassword());
		mu.setEmail(registrationRequest.getEmail());
		String activationCode = UUID.randomUUID().toString();
		mu.setActivationCode(activationCode);
		mu.setActive(false);
		try
		{
			dataService.add(MolgenisUser.ENTITY_NAME, mu);
		}
		catch (Exception e)
		{
			return response(false, "Registration failed. Please contact the developers.");
		}

		// send activation email
		try
		{
			String activationUriBase = null;
			if (StringUtils.isEmpty(servletRequest.getHeader("X-Forwarded-Host")))
			{
				activationUriBase = ServletUriComponentsBuilder.fromCurrentRequest().replacePath(URI + "/activate")
						.build().toUriString();
			}
			else
			{
				activationUriBase = servletRequest.getScheme() + "://" + servletRequest.getHeader("X-Forwarded-Host")
						+ URI + "/activate";
			}
			URI activationUri = java.net.URI.create(activationUriBase + '/' + activationCode);

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(registrationRequest.getEmail());
			mailMessage.setSubject("Registration Autobetes");
			mailMessage.setText("To activate your account, please visit " + activationUri);
			System.out.println(">> Message: " + mailMessage.getText());
			mailSender.send(mailMessage);
		}
		catch (Exception e)
		{
			Object typedId = dataService.getRepositoryByEntityName(MolgenisUser.ENTITY_NAME).getEntityMetaData()
					.getIdAttribute().getDataType().convert(mu.getIdValue());

			dataService.delete(MolgenisUser.ENTITY_NAME, typedId);

			System.err.println(">> ERRROR >> " + e);
			return response(false,
					"Registration failed. Sending email with activation link failed. Please contact the developers if "
							+ registrationRequest.getEmail() + " is really your email address.");
		}

		return response(
				true,
				"Registration successful! We have sent you an email with a link to activate your account. NB The email may have ended up in your spam folder.");
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Map<String, Object>> addEntity(@RequestBody TimestampLastUpdate timestamp,
			HttpServletRequest servletRequest)
	{
		
		return null;
	}
	
	
	@RequestMapping(value = "/update", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Map<String, Object>> update(@RequestBody TimestampLastUpdate timestamp,
			HttpServletRequest servletRequest)
	{
		List<Map<String, Object>> entities = new ArrayList<Map<String, Object>>();
		MolgenisUser user = getUserFromToken(TokenExtractor.getToken(servletRequest));
	
		//get events from a user with timestamps greater than timestamp of request
		Iterable<Entity> events = dataService.findAll(Event.ENTITY_NAME, new QueryImpl().eq(Event.OWNER, user).and().ge(Event.LASTCHANGED, timestamp.getTimestamp()));
		addEntitiesToList(entities, Event.ENTITY_NAME, events);
		
		//same for food event instances
		Iterable<Entity> foodEventInstances = dataService.findAll(FoodEventInstance.ENTITY_NAME, new QueryImpl().eq(FoodEventInstance.OWNER, user).and().ge(FoodEventInstance.LASTCHANGED, timestamp.getTimestamp()));
		addEntitiesToList(entities, FoodEventInstance.ENTITY_NAME, foodEventInstances);
		
		
		//same for activity event instances
		Iterable<Entity> activityEventinstances = dataService.findAll(ActivityEventInstance.ENTITY_NAME, new QueryImpl().eq(ActivityEventInstance.OWNER, user).and().ge(ActivityEventInstance.LASTCHANGED, timestamp.getTimestamp()));
		addEntitiesToList(entities, ActivityEventInstance.ENTITY_NAME, activityEventinstances);

		return entities;
	}
	

	

	/**
	 * Updates an entity using PUT
	 * 
	 * Example url: /api/v1/person/99
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */
	/*
	@RequestMapping(value = "/update", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Map<String, Object>> update(@RequestBody TimestampLastUpdate timestamp,
			HttpServletRequest servletRequest)
	{
		*/
	
	@RequestMapping(value = "/{entityName}/{id}", method = PUT)
	@ResponseStatus(OK)
	public void update(@PathVariable("entityName") String entityName, @PathVariable("id") Object id,
			@RequestBody Map<String, Object> entityMap, HttpServletRequest servletRequest)
	{
		//IN THE MAKING
		MolgenisUser user = getUserFromToken(TokenExtractor.getToken(servletRequest));
		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		Entity entity = toEntity(meta, entityMap);
		float entityLastChanged = (float)(entity.get("lastChanged"));
		
		Entity dbEntity = dataService.findOne(entityName, id);
		float dbEntityLastChanged = (float)(dbEntity.get("lastChanged"));
		
		if(entityLastChanged > dbEntityLastChanged){
			System.out.println("send entity more recently modified");
			updateInternal(entityName, id, entityMap);
		}
		else{
			System.out.println("db entity more recently modified");
		}
		
	}
	
	private void addEntitiesToList(List<Map<String, Object>> entities, String entityName, Iterable<Entity> events)
	{
		//convert Iterable<Event> to List<Map<String, Object>> in order to send as json
		EntityMetaData meta = dataService.getEntityMetaData(entityName);
				
		for (Entity entity : events)
		{
			entities.add(getEntityAsMap(entity, meta, null, null));
		}
		
	}
	
	//copied from restController
	private void updateInternal(String entityName, Object id, Map<String, Object> entityMap)
	{
		EntityMetaData meta = dataService.getEntityMetaData(entityName);
		if (meta.getIdAttribute() == null)
		{
			throw new IllegalArgumentException(entityName + " does not have an id attribute");
		}

		Entity existing = dataService.findOne(entityName, id);
		if (existing == null)
		{
			throw new UnknownEntityException("Entity of type " + entityName + " with id " + id + " not found");
		}

		Entity entity = toEntity(meta, entityMap);
		entity.set(meta.getIdAttribute().getName(), existing.getIdValue());

		dataService.update(entityName, entity);
	}
	
	// Creates a new MapEntity based from a HttpServletRequest
	//copied from restController
	private Entity toEntity(EntityMetaData meta, Map<String, Object> request)
	{
		Entity entity = new MapEntity();
		if (meta.getIdAttribute() != null) entity = new MapEntity(meta.getIdAttribute().getName());

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			String paramName = attr.getName();
			Object paramValue = request.get(paramName);
			Object value = toEntityValue(attr, paramValue);
			entity.set(attr.getName(), value);
		}

		return entity;
	}
	//copied from restcontroller
	private Object toEntityValue(AttributeMetaData attr, Object paramValue)
	{
		Object value = null;

		// Treat empty strings as null
		if ((paramValue != null) && (paramValue instanceof String) && StringUtils.isEmpty((String) paramValue))
		{
			paramValue = null;
		}

		if (paramValue != null)
		{
			if (attr.getDataType().getEnumType() == XREF || attr.getDataType().getEnumType() == CATEGORICAL)
			{
				value = dataService.findOne(attr.getRefEntity().getName(), paramValue);
				if (value == null)
				{
					throw new IllegalArgumentException("No " + attr.getRefEntity().getName() + " with id " + paramValue
							+ " found");
				}
			}
			else if (attr.getDataType().getEnumType() == MREF)
			{
				List<Object> ids = DataConverter.toObjectList(paramValue);
				if ((ids != null) && !ids.isEmpty())
				{
					Iterable<Entity> mrefs = dataService.findAll(attr.getRefEntity().getName(), ids);
					List<Entity> mrefList = Lists.newArrayList(mrefs);
					if (mrefList.size() != ids.size())
					{
						throw new IllegalArgumentException("Could not find all referencing ids for  " + attr.getName());
					}

					value = mrefList;
				}
			}
			else
			{
				value = DataConverter.convert(paramValue, attr);
			}
		}
		return value;
	}

	
	// Transforms an entity to a Map so it can be transformed to json
	// copied from restController
	private Map<String, Object> getEntityAsMap(Entity entity, EntityMetaData meta, Set<String> attributesSet,
			Map<String, Set<String>> attributeExpandsSet)
	{
		if (null == entity) throw new IllegalArgumentException("entity is null");

		if (null == meta) throw new IllegalArgumentException("meta is null");

		Map<String, Object> entityMap = new LinkedHashMap<String, Object>();
		

		// TODO system fields
		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			// filter fields
			if (attributesSet != null && !attributesSet.contains(attr.getName().toLowerCase())) continue;

			// TODO remove __Type from jpa entities
			if (attr.isVisible() && !attr.getName().equals("__Type"))
			{
				String attrName = attr.getName();
				FieldTypeEnum attrType = attr.getDataType().getEnumType();

				if (attrType == COMPOUND)
				{
					if (attributeExpandsSet != null && attributeExpandsSet.containsKey(attrName.toLowerCase()))
					{
						Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
						entityMap.put(attrName, new AttributeMetaDataResponse(meta.getName(), attr, subAttributesSet,
								null));
					}
					else
					{
						String attrHref = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(),
								attrName);
						entityMap.put(attrName, Collections.singletonMap("href", attrHref));
					}
				}
				else if (attrType == DATE)
				{
					Date date = entity.getDate(attrName);
					entityMap
							.put(attrName,
									date != null ? new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATE)
											.format(date) : null);
				}
				else if (attrType == DATE_TIME)
				{
					Date date = entity.getDate(attrName);
					entityMap
							.put(attrName,
									date != null ? new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME)
											.format(date) : null);
				}
				else if (attrType != XREF && attrType != CATEGORICAL && attrType != MREF)
				{
					entityMap.put(attrName, entity.get(attr.getName()));
				}
				else if ((attrType == XREF || attrType == CATEGORICAL) && attributeExpandsSet != null
						&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					Entity refEntity = entity.getEntity(attr.getName());
					if (refEntity != null)
					{
						Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
						EntityMetaData refEntityMetaData = dataService.getEntityMetaData(attr.getRefEntity().getName());
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								subAttributesSet, null);
						entityMap.put(attrName, refEntityMap);
					}
				}
				else if (attrType == MREF && attributeExpandsSet != null
						&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					EntityMetaData refEntityMetaData = dataService.getEntityMetaData(attr.getRefEntity().getName());
					Iterable<Entity> mrefEntities = entity.getEntities(attr.getName());

					Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
					List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
					for (Entity refEntity : mrefEntities)
					{
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								subAttributesSet, null);
						refEntityMaps.add(refEntityMap);
					}

					EntityPager pager = new EntityPager(0, new EntityCollectionRequest().getNum(),
							(long) refEntityMaps.size(), mrefEntities);

					String uri = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), attrName);
					EntityCollectionResponse ecr = new EntityCollectionResponse(pager, refEntityMaps, uri);
					entityMap.put(attrName, ecr);
				}
				else if ((attrType == XREF && entity.get(attr.getName()) != null)
						|| (attrType == CATEGORICAL && entity.get(attr.getName()) != null) || attrType == MREF)
				{
					// Add href to ref field
					Map<String, String> ref = new LinkedHashMap<String, String>();
					ref.put("href",
							String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), attrName));
					entityMap.put(attrName, ref);
				}

			}

		}

		return entityMap;
	}

	
	private Map<String, Object> response(boolean success, String msg)
	{
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("success", success);
		result.put("message", msg);

		return result;
	}
	
	private MolgenisUser getUserFromToken(String token)
	{
		//get token entity
		MolgenisToken tokenEntity = dataService.findOne(MolgenisToken.ENTITY_NAME, new QueryImpl().eq(MolgenisToken.TOKEN, token), MolgenisToken.class);
		//get user with token
		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.ID, tokenEntity.getMolgenisUser().getId()), MolgenisUser.class);
		return user;
	}
}
