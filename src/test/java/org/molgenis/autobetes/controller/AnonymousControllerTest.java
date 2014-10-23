package org.molgenis.autobetes.controller;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
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
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.token.MolgenisToken;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class AnonymousControllerTest{
	private static String ENTITY_NAME = "Event";
	private static Object ENTITY_ID = "1";
	private static String HREF_ENTITY = BASE_URI + "/" + ENTITY_NAME;
	private static String HREF_ENTITY_META = HREF_ENTITY + "/meta";
	private static String HREF_ENTITY_ID = HREF_ENTITY + "/"+ENTITY_ID;
	private static String TOKEN = "ABC";
	private MolgenisUser molgenisUser;
	private AnonymousController anonymousController;
	private DataService dataService;
	private MockMvc mockMvc;
	private List<Map<String, Object>> responseData = new ArrayList<Map<String, Object>>();
	private Entity entity;
	EntityMetaData entityMetaData;
	
	@BeforeMethod
	public void beforeMethod()
	{
		
		dataService = mock(DataService.class);
		anonymousController = new AnonymousController();
		
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
		MolgenisToken tokenEntity = new MolgenisToken();
		tokenEntity.setToken(TOKEN);
		
		//entity.set("xrefAttribute", entityXref);

		//Entity storedEntity = dataService.findOne(meta.getName(),
		//		new QueryImpl().eq(Event.OWNER, user).and().eq(Event.SID, entity.get(Event.SID)));// (meta.getName(),
		when(dataService.findOne(ENTITY_NAME,
				new QueryImpl().eq(MolgenisToken.TOKEN, TOKEN), MolgenisToken.class)).thenReturn(tokenEntity);
		
		when(dataService.findOne(ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.ID, user.getId()), MolgenisUser.class)).thenReturn(user);
		
		
		//Entity storedEntity = dataService.findOne(ENTITY_NAME,
		//		new QueryImpl().eq(Event.OWNER, user).and().eq(Event.SID, entity.get(Event.SID)));// (meta.getName(),

		
		/*
		when(dataService.getEntityNames()).thenReturn(Arrays.asList(ENTITY_NAME));
		when(dataService.getRepositoryByEntityName(ENTITY_NAME)).thenReturn(repo);

		when(dataService.findOne(ENTITY_NAME, ENTITY_ID)).thenReturn(entity);
		
		//when(dataService.findOne(ENTITY_NAME, new QueryImpl().eq(Event.OWNER, null))).thenReturn(entity);

		
		Query q = new QueryImpl().eq("name", "Piet").pageSize(10).offset(5);
		when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Arrays.asList(entity));
		*/
		
		
		
		DefaultAttributeMetaData attrName = new DefaultAttributeMetaData("name", FieldTypeEnum.STRING);
		attrName.setLookupAttribute(true);
		DefaultAttributeMetaData attrDType = new DefaultAttributeMetaData("DType", FieldTypeEnum.STRING);
		attrDType.setLookupAttribute(true);
		DefaultAttributeMetaData attrId = new DefaultAttributeMetaData("sId", FieldTypeEnum.STRING);
		attrId.setIdAttribute(true);
		attrId.setVisible(false);
		DefaultAttributeMetaData attrCId = new DefaultAttributeMetaData("cId", FieldTypeEnum.STRING);
		attrCId.setLookupAttribute(true);
		
		

		entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getAttribute("name")).thenReturn(attrName);
		when(entityMetaData.getAttribute("DType")).thenReturn(attrDType);
		when(entityMetaData.getAttribute("cId")).thenReturn(attrDType);
		when(entityMetaData.getIdAttribute()).thenReturn(attrId);
		
		when(entityMetaData.getAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId, attrDType, attrCId));
		when(entityMetaData.getAtomicAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId, attrDType, attrCId));
		when(entityMetaData.getName()).thenReturn(ENTITY_NAME);
		when(repo.getEntityMetaData()).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(entityMetaData);
		when(repo.getName()).thenReturn(ENTITY_NAME);

		//mockMvc = MockMvcBuilders.standaloneSetup(restController)
		//		.setMessageConverters(new GsonHttpMessageConverter(), new CsvHttpMessageConverter()).build();
		
	}
	
	
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
	@Test 
	public void getEntityAsMapTest(){
		
		Map<String, Object> returnedMap = anonymousController.getEntityAsMap(entity, entityMetaData, null, null);
		Map<String, Object> expected = new LinkedHashMap<String, Object>();
		
		expected.put("name", entity.get("name"));
		expected.put("DType", entity.get("DType"));
		expected.put("cId", entity.get("cId"));
		
		assertEquals(returnedMap, expected);
	}
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
