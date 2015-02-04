package org.molgenis.autobetes.controller;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.Updateable;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.security.token.MolgenisToken;
import org.molgenis.security.usermanager.UserManagerService;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration
public class AnonymousControllerTest extends AbstractTestNGSpringContextTests{
	
	@Configuration
	public static class Config extends WebMvcConfigurerAdapter
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
		@Bean
		public JavaMailSender javaMailSender()
		{
			return mock(JavaMailSender.class);
		}
		
		
	}
	
	@Autowired
	private DataService dataService;
	@Autowired
	private JavaMailSender mailSender;
	
	private AnonymousController anonymousController;
	
	private static String ENTITY_NAME = "TestEvent";
	private static Object ENTITY_ID = "1";
	private static String HREF_ENTITY = BASE_URI + "/" + ENTITY_NAME;
	private static String HREF_ENTITY_META = HREF_ENTITY + "/meta";
	private static String HREF_ENTITY_ID = HREF_ENTITY + "/"+ENTITY_ID;
	private static String TOKEN = "ABC";
	private MolgenisUser molgenisUser;
	
	private MockMvc mockMvc;
	private List<Map<String, Object>> responseData = new ArrayList<Map<String, Object>>();
	private Entity entity;
	private HttpServletRequest servletRequest;
	private static String TOKEN_HEADER = "x-molgenis-token";
	
	
	@BeforeMethod
	public void setUp()
	{
		anonymousController = new AnonymousController(dataService, mailSender);
		//dataService 
		//anonymousController =  mock(AnonymousController.class);
		servletRequest = mock(HttpServletRequest.class);
		
		Repository repo = mock(Repository.class, withSettings().extraInterfaces(Updateable.class, Queryable.class));

		Entity entityXref = new MapEntity("id");
		entityXref.set("id", ENTITY_ID);
		entityXref.set("name", "PietXREF");

		entity = new MapEntity("id");
		entity.set("sId", ENTITY_ID);
		entity.set("name", "Computeren");
		entity.set("DType", "Event");
		entity.set("cId", "100");
		
		
		MolgenisUser user = new MolgenisUser();
		user.setFirstName("John");
		user.setId(1);
		when(dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.ID, user.getId()), MolgenisUser.class)).thenReturn(user);
		
		
		
		DefaultAttributeMetaData attrName = new DefaultAttributeMetaData("name", FieldTypeEnum.STRING);
		attrName.setLookupAttribute(true);
		DefaultAttributeMetaData attrPrimaryKey = new DefaultAttributeMetaData("primaryKey", FieldTypeEnum.STRING);
		attrPrimaryKey.setLookupAttribute(true);
		DefaultAttributeMetaData attrId = new DefaultAttributeMetaData("id", FieldTypeEnum.STRING);
		attrId.setIdAttribute(true);
		
		DefaultAttributeMetaData attrOwner = new DefaultAttributeMetaData("owner", FieldTypeEnum.XREF);
		attrOwner.setLookupAttribute(true);
		DefaultAttributeMetaData attrDeleted = new DefaultAttributeMetaData("deleted", FieldTypeEnum.BOOL);
		attrDeleted.setLookupAttribute(true);
		DefaultAttributeMetaData attrTestInt = new DefaultAttributeMetaData("testInt", FieldTypeEnum.INT);
		attrTestInt.setLookupAttribute(true);
		
		

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		
		
		when(servletRequest.getHeader(TOKEN_HEADER)).thenReturn(TOKEN);
		when(entityMetaData.getAttribute("name")).thenReturn(attrName);
		when(entityMetaData.getAttribute("primaryKey")).thenReturn(attrPrimaryKey);
		when(entityMetaData.getAttribute("id")).thenReturn(attrId);
		when(entityMetaData.getAttribute("owner")).thenReturn(attrOwner);
		when(entityMetaData.getAttribute("deleted")).thenReturn(attrDeleted);
		when(entityMetaData.getAttribute("testInt")).thenReturn(attrTestInt);
		when(entityMetaData.getIdAttribute()).thenReturn(attrPrimaryKey);
		
		when(entityMetaData.getAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId, attrPrimaryKey, attrId, attrDeleted, attrTestInt,attrOwner));
		when(entityMetaData.getAtomicAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId, attrPrimaryKey, attrId, attrDeleted, attrTestInt,attrOwner));
		when(entityMetaData.getName()).thenReturn(ENTITY_NAME);
		when(repo.getEntityMetaData()).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(entityMetaData);
		when(repo.getName()).thenReturn(ENTITY_NAME);
		
		
		
		this.mockMvc = MockMvcBuilders.standaloneSetup(anonymousController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
		
		
		
		
		MolgenisToken tokenEntity = mock(MolgenisToken.class);
		when(tokenEntity.getMolgenisUser()).thenReturn(user);
		//MolgenisToken tokenEntity = dataService.findOne(MolgenisToken.ENTITY_NAME,
			//	  	new QueryImpl().eq(MolgenisToken.TOKEN, token), MolgenisToken.class);
		
		when(dataService.findOne(MolgenisToken.ENTITY_NAME,
					  	new QueryImpl().eq(MolgenisToken.TOKEN, TOKEN), MolgenisToken.class)).thenReturn(tokenEntity);
		
		List<Map<String, Object>> entitiesList = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("id", "asdf12");
		map.put("deleted", "1");
		map.put("lastchanged", "2");
		map.put("name", "japaleno peper");
		map.put("testInt", "2.0");
		entitiesList.add(map);
		
		Entity entity = new MapEntity();
		entity.set("id", "asdf12");
		entity.set("deleted", false);
		entity.set("lastchanged", 2);
		entity.set("name", "japaleno peper");
		entity.set("testInt", 2);
		
		Entity[] entityAsArray = {entity};
		Iterable<Entity> entityIterable = Arrays.asList(entityAsArray);
		when(dataService.findAll(ENTITY_NAME, new QueryImpl().eq(Event.OWNER, user))).thenReturn(entityIterable);
		
		
	}
	
	/*
	@Test
	public void addEntitiesToListTest(){
		
		HashSet<Integer> keys = new HashSet<Integer>();
		keys.add(1);
		ArrayList entitiesArray = new ArrayList();
		entitiesArray.add(entity);
		Iterable<Entity> entities = entitiesArray;
		List<Map<String, Object>> response = anonymousController.addEntitiesToList(responseData, entityMetaData, entities, keys);
		List<Map<String, Object>> expected = new ArrayList<Map<String, Object>>();
		Map<String, Object> entityAsMap = new LinkedHashMap<String, Object>();
		entityAsMap.put("name", entity.get("name"));
		entityAsMap.put("DType", entity.get("DType"));
		entityAsMap.put("cId", entity.get("cId"));
		expected.add(entityAsMap);
		assertEquals(response, expected);
		
		keys.remove(1);
		responseData.clear();
		response = anonymousController.addEntitiesToList(responseData, entityMetaData, entities, keys);
		entityAsMap.put(AnonymousController.NOTINREQUESTCONTENT, AnonymousController.TRUE);
		assertEquals(response, expected);
		
		
	}
	*/
	/*
	@Test 
	public void getEntityAsMapTest(){
		
		Map<String, Object> returnedMap = anonymousController.getEntityAsMap(entity, entityMetaData, null, null);
		Map<String, Object> expected = new LinkedHashMap<String, Object>();
		
		expected.put("name", entity.get("name"));
		expected.put("DType", entity.get("DType"));
		expected.put("cId", entity.get("cId"));
		
		assertEquals(returnedMap, expected);
	}*/
	
	/* TODO: remove! sync2 seems to be non-existent!!
	@Test
	public void testSync2() throws Exception{
			
		//test with testInt as a double, should be integer
		this.mockMvc.perform(post(AnonymousController.URI+"/sync2").header(TOKEN_HEADER, TOKEN)
				.content("[{id:'asdf12', name:'Piet', testInt: 2.565, lastchanged:'2', deleted:1}]")
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
		//test with deleted as a string undefined
		this.mockMvc.perform(post(AnonymousController.URI+"/sync2").header(TOKEN_HEADER, TOKEN)
				.content("[{id:'asdf12', name:'Piet', testInt: 2.0, lastchanged:'2', deleted:'undefined'}]")
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
		//test with deleted as undefined(without brackets)
		this.mockMvc.perform(post(AnonymousController.URI+"/sync2").header(TOKEN_HEADER, TOKEN)
				.content("[{id:'asdf12', name:'Piet', testInt: 2.0, lastchanged:'2', deleted:undefined}]")
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
		//test with testint being 0.1
		this.mockMvc.perform(post(AnonymousController.URI+"/sync2").header(TOKEN_HEADER, TOKEN)
				.content("[{id:'asdf12', name:'Piet', testInt: 0.1, lastchanged:'2', deleted:undefined}]")
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
		//test with entity without name
		this.mockMvc.perform(post(AnonymousController.URI+"/sync2").header(TOKEN_HEADER, TOKEN)
				.content("[{id:'asdf12', testInt: 2.0, lastchanged:'2', deleted:0}]")
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
		//test with entity without id
		this.mockMvc.perform(post(AnonymousController.URI+"/sync2").header(TOKEN_HEADER, TOKEN)
				.content("[{name:'fiets', testInt: 2.0, lastchanged:'2', deleted:0}]")
				.contentType(APPLICATION_JSON)).andExpect(status().isOk());
	}
	*/
	/*
	@Test
	public void iteratorWithoutReport() throws IOException
	{
		
		StringBuilder path = new StringBuilder();
		path.append(File.separatorChar).append("flowexport_test_gaflistfilerepository.csv");
		File file = new File(getClass().getResource(path.toString()).getFile());
		@SuppressWarnings("resource")
		GafListFileRepository gafListFileRepository = new GafListFileRepository(file, null, null, null);
		Iterator<Entity> it = gafListFileRepository.iterator();
		assertTrue(it.hasNext());
		Entity firstEntity = it.next();
		String barcode1 = firstEntity.getString("Barcode 1");
		String barcode = firstEntity.getString("barcode");
		String barcodeType = firstEntity.getString("barcodeType");
		assertEquals(barcode1, "AGI 1 AAGGTTCC");
		assertEquals(barcode, "AAGGTTCC");
		assertEquals(barcodeType, "AGI");
		
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertTrue(it.hasNext());
		it.next();
		assertFalse(it.hasNext());
		
	}
	*/




}
