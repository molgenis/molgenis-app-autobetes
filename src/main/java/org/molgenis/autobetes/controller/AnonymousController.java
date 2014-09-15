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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.lang.model.UnknownEntityException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.autobetes.autobetes.ActivityEvent;
import org.molgenis.autobetes.autobetes.ActivityEventInstance;
import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.autobetes.autobetes.EventInstance;
import org.molgenis.autobetes.autobetes.FoodEvent;
import org.molgenis.autobetes.autobetes.FoodEventInstance;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.rest.AttributeMetaDataResponse;
import org.molgenis.data.rest.EntityCollectionRequest;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.token.MolgenisToken;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
	public static final String TIME_STAMP_LAST_SYNC = "timeStampLastSync";
	public static final String NOTINREQUESTCONTENT = "notInRequestContent";
	public static final String TRUE = "True";
	public static final String FOOD = "Food";

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
			
			
			UserAuthority anonymousHomeAuthority = new UserAuthority();
			anonymousHomeAuthority.setMolgenisUser(mu);
			anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX
					+ AnonymousController.ID.toUpperCase());
			dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
			
			anonymousHomeAuthority = new UserAuthority();
			anonymousHomeAuthority.setMolgenisUser(mu);
			anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + HomeController.ID.toUpperCase());
			dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
			
		}
		catch (Exception e)
		{
			System.out.println("errore: "+ e);
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
			mailMessage.setFrom("dionkoolhaas@gmail.com");
			//System.out.println(mailMessage.toString());
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

	/**
	 * Updates an entity using PUT
	 * 
	 * Example url: /api/v1/person/99
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */
	@RequestMapping(value = "/sync", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<Map<String, Object>> sync(@RequestBody List<Map<String, Object>> entityMap,
			HttpServletRequest servletRequest)
	{
		System.out.println(entityMap.toString());
		// declare objects
		TimestampLastUpdate timeStampLastSync = new TimestampLastUpdate(0);// timestamp of the last sync of client,
		// send along in requestbody, if not it remains 0

		List<Map<String, Object>> responseData = new ArrayList<Map<String, Object>>();// response list that will be
																						// returned to client as a json
		MolgenisUser user = getUserFromToken(TokenExtractor.getToken(servletRequest));
		// metadata is used to convert entity to Map<String, Object> and vice versa
		EntityMetaData metaFoodEvent = dataService.getEntityMetaData(FoodEvent.ENTITY_NAME);
		EntityMetaData metaActivityEvent = dataService.getEntityMetaData(ActivityEvent.ENTITY_NAME);
		EntityMetaData metaFoodEventInstance = dataService.getEntityMetaData(FoodEventInstance.ENTITY_NAME);
		EntityMetaData metaActivityEventInstance = dataService.getEntityMetaData(ActivityEventInstance.ENTITY_NAME);
		// HashSet keys are used to determine if entity comes from the request list or from db
		HashSet<Integer> eventKeys = new HashSet<Integer>();
		HashSet<Integer> eventInstanceKeys = new HashSet<Integer>();
		// if a client defines an event and immediately(before syncing) an corresponding instance,
		// the client does not have the sId of the event and therefore no server foreign key(sEvent) for the instance.
		// Therefore cid(key) and sid(value) need to be in hashmap, so the foreign key(sEvent) can be declared
		HashMap<Integer, Integer> newEventSiDs = new HashMap<Integer, Integer>();
		// iterate request list

		iterateListRecursively(0, timeStampLastSync, user, entityMap, newEventSiDs, eventKeys, eventInstanceKeys,
				metaFoodEvent, metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
		// get entities from db and put these in response data
		getEntitiesFromDBAndAppendToResponseData(FoodEvent.ENTITY_NAME, user, timeStampLastSync.getTimestamp(),
				responseData, metaFoodEvent, eventKeys);
		getEntitiesFromDBAndAppendToResponseData(ActivityEvent.ENTITY_NAME, user, timeStampLastSync.getTimestamp(),
				responseData, metaActivityEvent, eventKeys);
		
		getEntitiesFromDBAndAppendToResponseData(FoodEventInstance.ENTITY_NAME, user, timeStampLastSync.getTimestamp(),
				responseData, metaFoodEventInstance, eventInstanceKeys);
		getEntitiesFromDBAndAppendToResponseData(ActivityEventInstance.ENTITY_NAME, user,
				timeStampLastSync.getTimestamp(), responseData, metaActivityEventInstance, eventInstanceKeys);
		System.out.println("response:"+ responseData);
		return responseData;

	}

	/*
	 * Retrieves collection of entities and calls addEntitiesToList method to compose response data.
	 */
	private void getEntitiesFromDBAndAppendToResponseData(String entityName, MolgenisUser user, long timeStampLastSync,
			List<Map<String, Object>> response, EntityMetaData meta, HashSet<Integer> keys)
	{
		Iterable<Entity> dbEntities = dataService.findAll(entityName,
				new QueryImpl().eq(Event.OWNER, user).and().ge(Event.LASTCHANGED, timeStampLastSync));
		addEntitiesToList(response, meta, dbEntities, keys);
	}

	/**
	 * Iterate List with objects recursively and process them. A recursive fashion is chosen because then the list can
	 * be iterated only once and with the assurance that the event entities will be processed first
	 * 
	 * @param index
	 * @param timeStampLastSync
	 * @param user
	 * @param entityMap
	 * @param newEventSiDs
	 * @param eventKeys
	 * @param eventInstanceKeys
	 * @param metaEvent
	 * @param metaFoodEventInstance
	 * @param metaActivityEventInstance
	 */
	private void iterateListRecursively(int index, TimestampLastUpdate timeStampLastSync, MolgenisUser user,
			List<Map<String, Object>> entityMap, HashMap<Integer, Integer> newEventSiDs, HashSet<Integer> eventKeys,
			HashSet<Integer> eventInstanceKeys, EntityMetaData metaFoodEvent, EntityMetaData metaActivityEvent, EntityMetaData metaFoodEventInstance,
			EntityMetaData metaActivityEventInstance)
	{

		if (entityMap.size() > index)
		{
			Map<String, Object> mapEntity = entityMap.get(index);
			if (mapEntity.containsKey("timeStampLastSync"))
			{
				// object is the timestamp
				timeStampLastSync
						.setTimestamp(Long.valueOf(mapEntity.get(TIME_STAMP_LAST_SYNC).toString()).longValue());
				iterateListRecursively(index + 1, timeStampLastSync, user, entityMap, newEventSiDs, eventKeys,
						eventInstanceKeys, metaFoodEvent, metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
			}
			else if (mapEntity.containsKey(Event.NAME))
			{
				
				// object is an event entity
				// events need to be processed first, so first process this object and then proceed iteration
				
				if(mapEntity.get(Event.EVENTTYPE).equals(FOOD)){
				
				processMapEntity(mapEntity, newEventSiDs, metaFoodEvent, user, eventKeys);
				iterateListRecursively(index + 1, timeStampLastSync, user, entityMap, newEventSiDs, eventKeys,
						eventInstanceKeys, metaFoodEvent, metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
			
				}
				else
				{
					System.out.println((String)mapEntity.get(Event.EVENTTYPE)+" is not:"+FOOD);
					// object is an event entity
					// events need to be processed first, so first process this object and then proceed iteration
					
					processMapEntity(mapEntity, newEventSiDs, metaActivityEvent, user, eventKeys);
					iterateListRecursively(index + 1, timeStampLastSync, user, entityMap, newEventSiDs, eventKeys,
							eventInstanceKeys, metaFoodEvent, metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
				
				}
				
			}
			
			else if (mapEntity.containsKey(ActivityEventInstance.INTENSITY))
			{
				// object is an activity entity
				// events need to be processed first, so first proceed iteration and then process this object
				iterateListRecursively(index + 1, timeStampLastSync, user, entityMap, newEventSiDs, eventKeys,
						eventInstanceKeys, metaFoodEvent, metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
				processMapEntity(mapEntity, newEventSiDs, metaActivityEventInstance, user, eventInstanceKeys);

			}
			else if (mapEntity.containsKey(FoodEventInstance.AMOUNT))
			{
				// object is an food entity
				// same as activity entity
				iterateListRecursively(index + 1, timeStampLastSync, user, entityMap, newEventSiDs, eventKeys,
						eventInstanceKeys, metaFoodEvent, metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
				processMapEntity(mapEntity, newEventSiDs, metaFoodEventInstance, user, eventInstanceKeys);

			}
		}

	}

	/**
	 * Makes entity from a Map<String, Object> and creates or updates entity in db
	 * 
	 * @param meta
	 * @param request
	 * @param user
	 * @return
	 */
	private void processMapEntity(Map<String, Object> mapEntity, HashMap<Integer, Integer> newEventSiDs,
			EntityMetaData meta, MolgenisUser user, HashSet<Integer> keys)
	{
		if (meta.getName() == FoodEventInstance.ENTITY_NAME || meta.getName() == ActivityEventInstance.ENTITY_NAME)
		{
			if (mapEntity.get(EventInstance.SEVENT) == null)
			{
				// Entity is an instance and does not have the server foreign key(sEvent), only the clients one.
				// This is the case when client defines an Event and also an Instance without being updated inbetween.
				// Foreign key resides in the hashmap newEventSiDs.
				
				int cEvent = (int) mapEntity.get(EventInstance.CEVENT);
				
				if(newEventSiDs.containsKey(cEvent)){
					mapEntity.replace(EventInstance.SEVENT, newEventSiDs.get(cEvent));
				}
				else{
					throw new RuntimeException("");
				}
			}
		}
		// make entity
		Entity entity = toEntity(meta, mapEntity, user);

		if (entity.get(Event.SID) == null)
		{
			// no sId means entity is new on server
			try{
				Iterable<Entity> entitiesWithSameLastChanged = dataService.findAll(meta.getName(),
						new QueryImpl().eq(Event.OWNER, user).and().eq(Event.LASTCHANGED, entity.get(Event.LASTCHANGED)));// (meta.getName(),
				if(entitiesWithSameLastChanged.iterator().hasNext()){
					//we assume that two entities with the same lastchanged are identitcal
					//set SID of entity with the one of found entity in db 
					entity.set(Event.SID, entitiesWithSameLastChanged.iterator().next().get(Event.SID));
					//update entity
					dataService.update(meta.getName(), entity);
				}
				else{
					dataService.add(meta.getName(), entity);
				}
			}catch (Exception e){
				//failed to add entity
			}finally{
				
			}
			
			if (meta.getName() == FoodEvent.ENTITY_NAME || meta.getName() == ActivityEvent.ENTITY_NAME)
			{
				// entity is a new event
				// put cid as a key and sid as a value in the hashmap
				newEventSiDs.put(entity.getInt(Event.CID), entity.getInt(Event.SID));
			}
		}
		else
		{
			// entity exists on server, determine which one is more recent
			// get entity
			Entity storedEntity = dataService.findOne(meta.getName(),
					new QueryImpl().eq(Event.OWNER, user).and().eq(Event.SID, entity.get(Event.SID)));// (meta.getName(),
			
			
			if (storedEntity == null)
			{
				 throw new RuntimeException("Entity of type " + meta.getName() + " with id " + entity.get(Event.SID) +" not found");
			}
			// compare timestamp of entity from request and entity from db
			if (storedEntity.getDouble(Event.LASTCHANGED) < entity.getDouble(Event.LASTCHANGED))
			{
				// entity received from app more recent than on server.
				dataService.update(meta.getName(), entity);
			}

		}
		// add key to hash set, in order to determine later on(by composing responseData) if a certain entity was in the
		// request
		// or was stored in db
		keys.add(entity.getInt(Event.SID));

	}

	/**
	 * Creates a new MapEntity based from a HttpServletRequest copied from restController and slightly modified
	 * 
	 * @param meta
	 * @param request
	 * @param user
	 * @return
	 */
	private Entity toEntity(EntityMetaData meta, Map<String, Object> request, MolgenisUser user)
	{
		Entity entity = new MapEntity();

		if (meta.getIdAttribute() != null) entity = new MapEntity(meta.getIdAttribute().getName());

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{

			String paramName = attr.getName();
			Object paramValue = request.get(paramName);
			if (paramName == Event.DELETED)
			{
				paramValue = convertDoubleToBoolean((double) paramValue);
			}
			Object value = toEntityValue(attr, paramValue);
			entity.set(attr.getName(), value);
		}

		entity.set(Event.OWNER, user);

		return entity;
	}

	private boolean convertDoubleToBoolean(double input)
	{
		if (input > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * copied from restcontroller
	 * 
	 * @param attr
	 * @param paramValue
	 * @return
	 */
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

	/**
	 * copied from restcontroller, slightly modified Transforms an entity to a Map so it can be transformed to json
	 * 
	 * @param entity
	 * @param meta
	 * @param attributesSet
	 * @param attributeExpandsSet
	 * @return
	 */

	public Map<String, Object> getEntityAsMap(Entity entity, EntityMetaData meta, Set<String> attributesSet,
			Map<String, Set<String>> attributeExpandsSet)
	{
		if (null == entity) throw new IllegalArgumentException("entity is null");

		if (null == meta) throw new IllegalArgumentException("meta is null");

		Map<String, Object> entityMap = new LinkedHashMap<String, Object>();

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
				else if (attrName == EventInstance.SEVENT)
				{
					Object sid = entity.getEntity(EventInstance.SEVENT).get(Event.SID);
					entityMap.put(attrName, sid);
				}
				else if (attrName == Event.OWNER)
				{
					// dont want to include owner data in response
					// do nothing
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

	/**
	 * Declares user according to the given token
	 * 
	 * @param token
	 * @return
	 */
	public MolgenisUser getUserFromToken(String token)
	{
		// get token entity
		MolgenisToken tokenEntity = dataService.findOne(MolgenisToken.ENTITY_NAME,
				new QueryImpl().eq(MolgenisToken.TOKEN, token), MolgenisToken.class);
		return tokenEntity.getMolgenisUser();
	}

	/**
	 * Iterate list with entities retrieved from db, and add this to responsedata
	 * 
	 * @param responseData
	 * @param meta
	 * @param events
	 * @param keys
	 */
	public List<Map<String, Object>> addEntitiesToList(List<Map<String, Object>> responseData, EntityMetaData meta,
			Iterable<Entity> events, HashSet<Integer> keys)
	{
		for (Entity entity : events)
		{
			Map<String, Object> entityAsMap = getEntityAsMap(entity, meta, null, null);
			if (keys.contains(entity.getInt(Event.SID)) == false)
			{
				// entity is not from requestbody but was allready stored in db
				// necessary to indicate this for the client
				entityAsMap.put(NOTINREQUESTCONTENT, TRUE);
			}
			responseData.add(entityAsMap);
		}
		return responseData;

	}

}
