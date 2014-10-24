package org.molgenis.autobetes.controller;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE_TIME;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.autobetes.controller.HomeController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.rest.AttributeMetaDataResponse;
import org.molgenis.data.rest.EntityCollectionRequest;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends MolgenisPluginController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String BASE_URI = "";

	@Autowired
	private DataService dataService;

	@Autowired
	private JavaMailSender mailSender;

	public HomeController()
	{
		super(URI);
	}

	@RequestMapping
	public String init()
	{
		return "view-home";
	}

	

	
}
