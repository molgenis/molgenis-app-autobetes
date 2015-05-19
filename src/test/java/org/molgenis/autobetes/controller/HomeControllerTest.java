package org.molgenis.autobetes.controller;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.testng.Assert.*;

import org.testng.annotations.Test;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.autobetes.BgSensor;
import org.molgenis.autobetes.autobetes.IdentificationServer;
import org.molgenis.autobetes.autobetes.TimeChange;
import org.molgenis.autobetes.autobetes.UserInfo;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;

@WebAppConfiguration
@ContextConfiguration
public class HomeControllerTest 
{


	private DataService dataService = mock(DataService.class);
	private FileStore filestore = mock(FileStore.class);
	
	private HomeController homeController;
	ArrayList<UserInfo> userInfoArray = new ArrayList<UserInfo>();
	Iterable<UserInfo> userInfoIterable = mock(Iterable.class);
	Iterator<UserInfo> userInfoIterator = mock(Iterator.class);
	
	private Long HOURS_IN_MILLISEC = 1000l*60l*60l;
	UserInfo userInfo = mock(UserInfo.class);
	MolgenisUser molgenisUser = mock(MolgenisUser.class);
	
	@BeforeMethod
	public void setUp()
	{
		homeController = new HomeController(dataService, filestore);
		when(dataService.findAll(UserInfo.ENTITY_NAME, 
				new QueryImpl().eq(UserInfo.OWNER, molgenisUser)
				.and().sort(Direction.ASC, UserInfo.LASTCHANGED), UserInfo.class)).thenReturn(userInfoIterable);
		when(userInfoIterable.iterator()).thenReturn(userInfoIterator);
		when(userInfoIterator.hasNext()).thenReturn(true,false,true,false,true,false,true,false);
		when(userInfoIterator.next()).thenReturn(userInfo);
		when(userInfo.getLastchanged()).thenReturn(1407402485l);
		when(userInfo.getTimeOffset()).thenReturn(2);
		when(dataService.findAll(TimeChange.ENTITY_NAME, new QueryImpl().eq(TimeChange.OWNER, molgenisUser).and().sort(Direction.ASC, TimeChange.UNIXTIMEORIGINAL), TimeChange.class)).thenReturn(new ArrayList<TimeChange>());
		
	}
	
	@Test
	public void performTimeCorrectionTest(){
		
		System.out.println("Test 1:Empty arrays");
		//Make empty arrays
		List<IdentificationServer> tsToBeCorrected = new ArrayList<IdentificationServer>();
		List<TimeChange> timeChangeList = new ArrayList<TimeChange>();
		List<IdentificationServer> response = homeController.performTimeCorrection(dataService, molgenisUser, tsToBeCorrected, timeChangeList);
		List<IdentificationServer> expected = new ArrayList<IdentificationServer>();
		assertEquals(response, expected);
		
		System.out.println("Test 2: One ts before first record and one after");
		//add one before(=1400000000l) and one after (=1500000000l)
		tsToBeCorrected.add(newIS(1400000000l));
		tsToBeCorrected.add(newIS(1500000000l));
		//same with expected + we expect with a ts of 1500000000l a unixtimecorrected of 1500000000l-(HOURSINMILLISEC*2
		expected.add(newIS(1400000000l));
		IdentificationServer ex2 = newIS(1500000000l);
		ex2.setUnixtimeCorrected(1500000000l-(HOURS_IN_MILLISEC*2));
		expected.add(ex2);
		response = homeController.performTimeCorrection(dataService, molgenisUser, tsToBeCorrected, timeChangeList);
		assertEquals(response, expected);
		
		System.out.println("Test 3: With one timechange of -1 hour in csv. ts's before and after this timechange");
		//make a timechange
		TimeChange tc = new TimeChange();
		tc.setUnixtimeOriginal(1600000000l);
		tc.setNewTime((1600000000l-HOURS_IN_MILLISEC));
		timeChangeList.add(tc);
		//add one ts after this timechange
		tsToBeCorrected.add( newIS(1700000000l));
		//same with expected + we expect after the timechange an offset of 1 hour(instead of 2)
		IdentificationServer ex3 = newIS(1700000000l);
		ex3.setUnixtimeCorrected(1700000000l-HOURS_IN_MILLISEC);
		expected.add(ex3);
		response = homeController.performTimeCorrection(dataService, molgenisUser, tsToBeCorrected, timeChangeList);
		assertEquals(response, expected);
		
		System.out.println("Test 4: With a ts in range of the timechange of test 3");
		//add ts within range
		tsToBeCorrected.add( newIS(1600000000l-HOURS_IN_MILLISEC+10));
		//we expect this ts to be removed, so nothing to add to expected
		response = homeController.performTimeCorrection(dataService, molgenisUser, tsToBeCorrected, timeChangeList);
		assertEquals(response, expected);
	}
	
	private IdentificationServer newIS(Long originalTS)
	{
		IdentificationServer is = new IdentificationServer();
		is.setUnixtimeOriginal(originalTS);
		return is;
	}
	
}
