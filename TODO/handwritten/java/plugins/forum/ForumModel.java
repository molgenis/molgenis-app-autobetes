/* Date:        August 23, 2011
 * Template:	EasyPluginModelGen.java.ftl
 * generator:   org.molgenis.generators.ui.EasyPluginModelGen 4.0.0-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package plugins.forum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lmd.ConsumedProduct;
import lmd.Food;
import lmd.LmdUser;
import lmd.Nutrients;
import lmd.Post;
import lmd.Product;
import lmd.Topic;
import lmd.VitaminDayFigures;
import lmd.WeightedFood;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.ui.EasyPluginModel;

/**
 * GenericWizardModel takes care of all state and it can have helper methods to
 * query the database. It should not contain layout or application logic which
 * are solved in View and Controller.
 * 
 * @See org.molgenis.framework.ui.ScreenController for available services.
 */
public class ForumModel extends EasyPluginModel
{
	// a system veriable that is needed by tomcat
	private static final long serialVersionUID = 1L;
	public Forum controller;

	public final Constants constants = new Constants();
	public final String FOOD_VIEW = "FoodView.ftl";
	public final String FOOD_VIEW_TAB_FOOD = "FOOD_VIEW_TAB_FOOD";
	public final String FOOD_VIEW_TAB_EXERCISE = "FOOD_VIEW_TAB_EXERCISE";
	public final String FOOD_VIEW_TAB_ADVICE = "FOOD_VIEW_TAB_ADVICE";
	public final String FOOD_VIEW_TAB_VITAMIN = "FOOD_FOOD_TAB_VITAMIN";
	public final String DISH_VIEW = "DishView.ftl";
	public final String DISH_VIEW_TAB_OVERVIEW = "DishViewTabOverview.ftl";
	public final String DISH_VIEW_TAB_EDIT = "DishViewTabEdit.ftl";
	public final String VOEDINGSDAGBOEK_VIEW = "VoedingsdagboekView.ftl";
	public final String ADDTOPIC_VIEW = "AddTopicView.ftl";
	public final String TOPIC_VIEW = "TopicView.ftl";
	public final String MESSAGE_VIEW = "MessageView.ftl";
	public final String MY_DATA_VIEW = "MyDataView.ftl";
	public final String DIABETES_VIEW = "DiabetesView.ftl";
	public final String HELP_VIEW = "HelpView.ftl";
	public final String DATE_FULL = "DATE_FULL";
	public final String DATE_ONLY_FULL = "DATE_ONLY_FULL";
	public final String DATE_DETAILED = "DATE_DETAILED";
	public final String DATE_SHORT = "DATE_SHORT";
	public final String DATE_DASHED = "DATE_DASHED";
	public final String DATE_DASHED_TIME = "DATE_DASHED_TIME";
	public final String DATE_DASHED_FIELD_INPUT = "DATE_DASHED_FIELD_INPUT";
	public final String DATE_YEAR_MONTH = "DATE_YEAR_MONTH";
	public final String DATE_MONTH_YEAR = "DATE_MONTH_YEAR";

	private final String GENDER_MALE = "male";
	private final String GENDER_FEMALE = "female";
	private final String GENDER_MALE_DUTCH = "man";
	private final String GENDER_FEMALE_DUTCH = "vrouw";
	private final String GENDER_DEFAULT = GENDER_FEMALE;
	private final String AGE_DEFAULT = "40";
	private final String DATE_DEFAULT = "01-01-1973";

	private String screen = VOEDINGSDAGBOEK_VIEW; // which *.ftl to load?
	private String screenPrevious = VOEDINGSDAGBOEK_VIEW; // which *.ftl to
															// load?
	private String screenFoodTab = FOOD_VIEW_TAB_FOOD; // which tab to show?
	private String screenDishTab = DISH_VIEW_TAB_OVERVIEW;

	private final Set<String> HELP_VIEW_TAB_SET = new HashSet<String>(Arrays.asList("ProdNA", "Amount"));
	private String screenHelpTab = HELP_VIEW_TAB_SET.iterator().next();

	public final String FOOD_TYPE_CONSUMPTION = "consumption";
	public final String FOOD_TYPE_DISH = "dish";
	
	public final List<String> dishCategoryDefaultList = new ArrayList<String>(Arrays.asList("Dessert", "Diner", "Drankje", "Lunch", "Ontbijt", "Salade", "Soep", "Tussendoortje", "Voorgerecht"));

	public LmdUser lmdUser = null; // not logged in
	private String forumNameList = "[ ]"; // list with current forum names in db
											// and is set when 'register view is
											// set'
	private List<Topic> topicList = new ArrayList<Topic>();
	private Topic currentTopic = null;
	private List<Post> postList = new ArrayList<Post>();
	private List<Post> fullPostList = new ArrayList<Post>();
	private Integer citePostId = null; // null means no post to be cited
	private Integer editPostId = null; // null means no post to be edited
	private Map<Integer, LmdUser> lmdUserMap = new LinkedHashMap<Integer, LmdUser>();
	private Map<Integer, Post> postMap = new LinkedHashMap<Integer, Post>();

	// food
	private List<ConsumedProduct> consumedProductList = new ArrayList<ConsumedProduct>();
	private List<WeightedFood> consumptionList = new ArrayList<WeightedFood>();
	List<Food> dishList = new ArrayList<Food>();
	public Food currentDish = null; // null means no current dish
	public List<WeightedFood> currentDishWeightedFood = new ArrayList<WeightedFood>(); // value ignored if null == currentDish
	public Nutrients dishNutrients100grams = null; // value ignored if null == currentDish

	// alerts
	private Alert alert = new Alert(this);

	// figures
	private VitaminDayFigures figs = new VitaminDayFigures();
	public Plot vitamine;
	public Plot vitaminePlus;

	private Login login = null;
	private java.util.Date foodDate = new Date();

	public boolean dirtyVitaminPlots = false; // if dirty, then recalc plots

	public Help helpItems = new Help();

	public ForumModel(Forum controller)
	{
		// each Model can access the controller to notify it when needed.
		super(controller);
		this.controller = controller;

		vitamine = new Plot(this, "myres/img/figVitaminesEmpty.png");
		vitaminePlus = new Plot(this, "myres/img/figHealthDefault.png");
	}

	public List<String> getDishCategoryDefaultList()
	{
		return this.dishCategoryDefaultList;
	}
	
	public String getDishName()
	{
		String name = this.currentDish.getName();
		return (null == name ? "" : name);
	}
	
	public Integer getDishId()
	{
		Integer id = this.currentDish.getId();
		return (null == id ? -1 : id);
	}
	
	public String getDishCategory()
	{
		String dishCategory = this.currentDish.getDishCategory();
		return (null == dishCategory ? "" : dishCategory);
	}
	
	public Food getDish()
	{
		return this.currentDish;
	}

	public String getFoodDateDashed()
	{
		return niceDate(this.foodDate, DATE_DASHED);
	}

	public String getFoodDateFull()
	{
		return niceDate(this.foodDate, DATE_ONLY_FULL);
	}

	public String getFoodDateDB()
	{
		return niceDate(this.foodDate, DATE_DASHED_FIELD_INPUT);
	}

	public void setFoodDate(Date newFoodDate)
	{
		this.foodDate = newFoodDate;
	}

	public Date getFoodDateDate()
	{
		return this.foodDate;
	}

	public Integer getNTopics()
	{
		return 1;
	}

	public Integer getTopicNPosts(Integer index)
	{
		return this.topicList.size();
	}

	public String getTopicTitle(Integer index)
	{
		return this.topicList.get(index).getTitle().toString();
	}

	public String getTopicAuthor(Integer index)
	{
		return this.topicList.get(index).getLmdUser_ForumName();
	}

	/**
	 * When was last reply in this topic and by whom?
	 * 
	 * @param topicId
	 * @return
	 */
	public String getLastReplyAuthorDate(Integer topicId)
	{
		Post lastPost = getLastPost(topicId);

		return niceDate(lastPost.getCreated(), DATE_SHORT) + " / " + lastPost.getLmdUser_ForumName();
	}

	private Post getLastPost(Integer topicId)
	{
		Post lastPost = new Post();
		lastPost.setCreated(new Date(0));

		Iterator<Post> it = fullPostList.iterator();
		while (it.hasNext())
		{
			Post p = it.next();
			if (p.getTopic_Id().equals(topicId) && p.getCreated().after(lastPost.getCreated()))
			{
				lastPost = p;
			}
		}

		return lastPost;
	}

	public String getLastMessage(Integer topicId)
	{
		String message = getLastPost(topicId).getMessage();

		// remove all between blockquotes and replace by [..] or so?
		final Pattern pattern = Pattern.compile("(.*)<BLOCKQUOTE>(.*)</BLOCKQUOTE>(.*)", Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(message);
		if (matcher.find())
		{
			message = matcher.group(1) + " [ ... ]\n" + matcher.group(3);
		}

		return message;
	}

	public Topic getCurrentTopic()
	{
		return this.currentTopic;
	}

	public void setScreen(String screenName)
	{
		this.screenPrevious = this.screen;
		this.screen = screenName;
	}

	public void toPreviousScreen()
	{
		String tmp = this.screenPrevious;
		this.screenPrevious = this.screen;
		this.screen = tmp;
	}

	public String getScreen()
	{
		return this.screen;
	}

	public void setScreenDishTab(String tab)
	{
		this.screenDishTab = tab;
	}

	public void setScreenTabHelp(String question)
	{
		if (HELP_VIEW_TAB_SET.contains(question))
		{
			this.screenHelpTab = question;
		}
	}

	public void setScreenFoodTab(String tab)
	{
		this.screenFoodTab = tab;
	}

	public String getScreenTabFood()
	{
		return this.screenFoodTab;
	}

	public String getScreenTabDish()
	{
		return this.screenDishTab;
	}

	public boolean isScreenTabFoodFood()
	{
		return this.FOOD_VIEW_TAB_FOOD.equals(getScreenTabFood());
	}

	public boolean isScreenTabFoodExercise()
	{
		return this.FOOD_VIEW_TAB_EXERCISE.equals(getScreenTabFood());
	}

	public boolean isScreenTabFoodVitamin()
	{
		return this.FOOD_VIEW_TAB_VITAMIN.equals(getScreenTabFood());
	}

	public boolean isScreenTabFoodAdvice()
	{
		return this.FOOD_VIEW_TAB_ADVICE.equals(getScreenTabFood());
	}

	public boolean isScreenTabDishOverview()
	{
		return this.DISH_VIEW_TAB_OVERVIEW.equals(getScreenTabDish());
	}

	public boolean isScreenTabDishEdit()
	{
		return this.DISH_VIEW_TAB_EDIT.equals(getScreenTabDish());
	}

	public String getScreenDishTabEdit()
	{
		return this.DISH_VIEW_TAB_EDIT;
	}

	public void setForumNameList(String forumNameList)
	{
		this.forumNameList = forumNameList;
	}

	public String getForumNameList()
	{
		return this.forumNameList;
	}

	public boolean isVoedingsdagboekView()
	{
		return VOEDINGSDAGBOEK_VIEW.equals(this.getScreen());
	}

	public boolean isFoodView()
	{
		return FOOD_VIEW.equals(this.getScreen());
	}

	public boolean isDiabetesView()
	{
		return DIABETES_VIEW.equals(this.getScreen());
	}

	public boolean isHelpView()
	{
		return HELP_VIEW.equals(this.getScreen());
	}

	public boolean isForumView()
	{
		return ADDTOPIC_VIEW.equals(this.getScreen()) || TOPIC_VIEW.equals(this.getScreen()) || MESSAGE_VIEW.equals(this.getScreen());
	}

	public boolean isMyDataView()
	{
		return MY_DATA_VIEW.equals(this.getScreen());
	}

	public boolean isLoggedIn()
	{
		return this.lmdUser != null;
	}

	public LmdUser getLmdUser()
	{
		return this.lmdUser;
	}

	public String getUserName()
	{
		return this.lmdUser.getFirstName() + " " + this.lmdUser.getLastName();
	}

	public String getForumName()
	{
		if (this.isLoggedIn())
			return this.lmdUser.getForumName();
		else
			return "";
	}

	public String getSensorDate(Integer postId)
	{
		Date d = this.lmdUserMap.get(this.postMap.get(postId).getLmdUser_Id()).getSensorDate();
		return (d == null ? "" : niceDate(d, DATE_MONTH_YEAR));
	}

	public String getPumpDate(Integer postId)
	{
		Date d = this.lmdUserMap.get(this.postMap.get(postId).getLmdUser_Id()).getPumpDate();
		return (d == null ? "" : niceDate(d, DATE_MONTH_YEAR));
	}

	public String getDiabetesDate(Integer postId)
	{
		Date d = this.lmdUserMap.get(this.postMap.get(postId).getLmdUser_Id()).getDiabetesDate();
		return (d == null ? "" : niceDate(d, DATE_MONTH_YEAR));
	}

	public String getBornDate(Integer postId)
	{
		Date d = this.lmdUserMap.get(this.postMap.get(postId).getLmdUser_Id()).getBornDate();
		return (d == null ? "" : niceDate(d, DATE_MONTH_YEAR));
	}

	public String getEmailOfPost(Integer postId)
	{
		return getMD5Hash(this.lmdUserMap.get(this.postMap.get(postId).getLmdUser_Id()).getEmail());
	}

	public String getChangeAuthorOfPost(Integer postId)
	{
		return this.lmdUserMap.get(this.postMap.get(postId).getChangedBy_Id()).getForumName();
	}

	public String getChangeDateOfPost(Integer postId)
	{
		return niceDate(this.postMap.get(postId).getChangeDate(), DATE_DASHED_TIME);
	}

	public boolean isMyPost(Integer postId)
	{
		if (!this.isLoggedIn())
		{
			return false;
		} else
		{
			return this.getLmdUser().getId().equals(this.postMap.get(postId).getLmdUser_Id());
		}
	}

	public void setTopicList(List<Topic> find)
	{
		this.topicList = find;
	}

	public List<Topic> getTopicList()
	{
		return this.topicList;
	}

	public void setCurrentTopic(Topic t)
	{
		this.currentTopic = t;
	}

	private Post clonePost(Post p)
	{
		Post newp = new Post();
		newp.setMessage(p.getMessage());
		newp.setTopic(p.getTopic());
		newp.setLmdUser_ForumName(p.getLmdUser_ForumName());
		newp.setId(p.getId());
		newp.setCreated(p.getCreated());

		newp.setChangedBy(p.getChangedBy());
		newp.setChangeDate(p.getChangeDate());
		newp.setChangePercentage(p.getChangePercentage());
		newp.setChangeReason(p.getChangeReason());

		return newp;
	}

	/**
	 * Return post list so that it can be displayed in HTML properly, i.e. \n
	 * replaced with <BR>
	 * 
	 * @return
	 */
	public List<Post> getPostList()
	{

		List<Post> htmlPostList = new ArrayList<Post>();
		Iterator<Post> it = this.postList.iterator();
		while (it.hasNext())
		{
			Post p = clonePost(it.next());
			p.setMessage(p.getMessage().replaceAll("\r\n", "<BR>"));
			p.setMessage(p.getMessage().replaceAll("\n", "<BR>"));
			htmlPostList.add(p);
		}

		return htmlPostList;
	}

	public void setPostList(List<Post> postList)
	{
		this.postList = postList;
	}

	public void setFullPostList(List<Post> fullPostList)
	{
		this.fullPostList = fullPostList;
	}

	public Integer getCitePostId()
	{
		return this.citePostId;
	}

	/**
	 * Return cited post; null if none-existent
	 * 
	 * @return
	 */
	public Post getCitePost()
	{
		Post p = new Post();
		p.setMessage("Er gaat iets fout met het vinden van het bericht dat u wilt citeren!");

		boolean found = false;
		Iterator<Post> it = this.postList.iterator();
		while (it.hasNext() && !found)
		{
			Post testPost = it.next();
			found = getCitePostId().equals(testPost.getId());
			if (found)
			{
				try
				{
					p.set(testPost.getValues());
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return p;
	}

	public boolean isCitation()
	{
		return null != this.citePostId;
	}

	public void setCitePostId(Integer citePostId)
	{
		this.citePostId = citePostId;
	}

	public Post getPost(Integer id)
	{
		return this.postMap.get(id);
	}

	public String getEditMessage()
	{
		String msg = getPost(getEditPostId()).getMessage();

		// TODO!!!
		// parse msg

		Pattern pattern = Pattern.compile("(.*)<BLOCKQUOTE>(.*)</BLOCKQUOTE>(.*)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find())
		{
			// before <BLOCKQ> in </BLOCKQ> after
			// from in: remove <A HREF=?>...</A>
			// before [begin] in [eind] after

			String before = matcher.group(1);
			String in = matcher.group(2);
			String after = matcher.group(3);

			pattern = Pattern.compile("(.*)<A HREF=.*>(.*)</A>", Pattern.DOTALL);
			matcher = pattern.matcher(in);

			if (matcher.find())
			{
				in = matcher.group(1);
			}

			return before + "[begin]" + in + "[eind]" + after;
		} else
		{
			// if no <BLOCKQ> then return msg
			return msg;
		}
	}

	public boolean isEditing()
	{
		return null != this.editPostId;
	}

	public void setEditPostId(Integer editPostId)
	{
		this.editPostId = editPostId;
	}

	public Integer getEditPostId()
	{
		return this.editPostId;
	}

	public String getPostCitation()
	{
		Post p = new Post();
		p.setLmdUser_ForumName("Forum user");
		p.setMessage("Geen citatie gevonden. Er gaat iets mis. Neem contact op met admin, svp.");
		p.setCreated(new java.util.Date());

		Post cp = getCitePost();
		if (null != cp)
			p = cp;

		// - replace all between <BLOCKQOUTE> with '[..]'
		final Pattern pattern = Pattern.compile("(.*)<BLOCKQUOTE>(.*)</BLOCKQUOTE>(.*)", Pattern.DOTALL);
		final Matcher matcher = pattern.matcher(p.getMessage());
		if (matcher.find())
		{
			// The message you want to cite contains a Citation (blockquote)
			// replace that with [..]
			p.setMessage(matcher.group(1) + "[ ... ]\n" + matcher.group(3));
		}

		// TODO replace with consts: BEGIN
		return "[begin]" + p.getMessage() + "[eind]";
		// return prefix + p.getMessage().replaceAll("\n", "<BR>") + postfix;
	}

	private String niceMonth(Integer m)
	{
		List<String> months = new ArrayList<String>(Arrays.asList("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec"));

		return months.get(m);
	}

	/**
	 * 
	 * @param date
	 * @param type
	 *            = [short, longer, full]
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public String niceDate(java.util.Date date, String type)
	{
		if (null == date)
			return "";

		String minutes = "XX";
		String hours = "XX";

		boolean timeAvailable = true;
		try
		{
			minutes = String.format("%02d", date.getMinutes()); // 0..59
			hours = String.format("%02d", date.getHours()); // 0..23
		} catch (Exception e)
		{
			timeAvailable = false;
		}

		String day = String.format("%02d", date.getDate()); // 1..31
		String month = String.format("%02d", date.getMonth() + 1); // 1..12
		String niceMonth = niceMonth(date.getMonth()); // Jan, Feb, ...
		String year = Integer.toString(date.getYear() + 1900);

		Date today = new Date();

		boolean sameDay = today.getDate() == date.getDate();
		boolean sameMonth = today.getMonth() == date.getMonth();
		boolean sameYear = today.getYear() == date.getYear();

		String time = hours + ":" + minutes;
		if (DATE_MONTH_YEAR.equals(type))
		{
			return month + "-" + year;
		} else if (DATE_YEAR_MONTH.equals(type))
		{
			return year + "-" + month;
		} else if (DATE_DASHED_FIELD_INPUT.equals(type))
		{
			return year + "-" + month + "-" + day;
		} else if (DATE_DASHED.equals(type))
		{
			return day + "-" + month + "-" + year;
		} else if (DATE_DASHED_TIME.equals(type))
		{
			return day + "-" + month + "-" + year + (timeAvailable ? " " + time : "");
		} else if (DATE_FULL.equals(type))
		{
			return day + " " + niceMonth + " " + year + (timeAvailable ? ", om " + time : "");
		} else if (DATE_ONLY_FULL.equals(type))
		{
			return day + " " + niceMonth + " " + year;
		} else
		{
			if (sameYear && sameMonth && sameDay)
			{
				return time;
			} else if (sameYear)
			{
				return day + " " + niceMonth + (DATE_DETAILED.equals(type) && timeAvailable ? ", om " + time : "");
			} else
			{
				return day + " " + niceMonth + " " + year + (DATE_DETAILED.equals(type) && timeAvailable ? ", om " + time : "");
			}
		}
	}

	public String niceMessageDate(Date date)
	{
		return niceDate(date, DATE_DETAILED);
	}

	public String inputDate(Date date)
	{
		if (null == date)
			return "";

		return niceDate(date, DATE_DASHED);
	}

	@SuppressWarnings("unchecked")
	public List<Topic> sortTopicList(List<Topic> list)
	{
		List<SortableTopic> sortableTopicList = new ArrayList<SortableTopic>();

		Iterator<Topic> it = list.iterator();
		while (it.hasNext())
		{
			Topic t = it.next();

			SortableTopic st = new SortableTopic(t, getLastPost(t.getId()));

			sortableTopicList.add(st);
		}

		Collections.sort(sortableTopicList);
		Collections.reverse(sortableTopicList);

		List<Topic> sorted = new ArrayList<Topic>();
		for (SortableTopic st : sortableTopicList)
		{
			sorted.add(st.getTopic());
		}

		return sorted;
	}

	public void setLmdUserMap(List<LmdUser> list)
	{
		this.lmdUserMap.clear();

		for (LmdUser fu : list)
			this.lmdUserMap.put(fu.getId(), fu);
	}

	public void setPostMap(List<Post> list)
	{
		this.postMap.clear();

		for (Post p : list)
			this.postMap.put(p.getId(), p);
	}

	public void setAlert(String messageType)
	{
		this.alert.setMessageType(messageType);
	}

	public Alert getAlert()
	{
		return this.alert;
	}

	private String MD5(String input)
	{
		try
		{
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(input.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
			{
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e)
		{
		}
		return null;
	}

	public String getMD5HashCurrentLmdUser()
	{
		if (this.isLoggedIn())
			return getMD5Hash(getLmdUser().getEmail());
		else
			return "anonymous";
	}

	public String getMD5Hash(String email)
	{
		if (null != email)
			return MD5(email.trim().toLowerCase());
		else
			return "anonymous";
	}

	public List<WeightedFood> getCurrentConsumptionList()
	{
		return this.consumptionList;
	}
	
	public List<WeightedFood> getCurrentDishWeightedFood()
	{
		return this.currentDishWeightedFood;
	}
	
	/**
	 * Fake that we ate one serving of each of my dishes
	 * to nicely present an overview of my dishes
	 * @return
	 */
	public List<WeightedFood> getDishListAsWeightedFood()
	{
		List<WeightedFood> consumedDishList = new ArrayList<WeightedFood>();
		for (Food dish : this.dishList)
		{
			WeightedFood wf = new WeightedFood();
			
			// set the id of dish. If dish is clicked we know which to edit
			wf.setId(dish.getId());
			
			wf.setFood(dish);
			
			Double weight = dish.getUnitWeight();
			wf.setWeight(weight);
			
			Nutrients nutrientsTotal = this.controller.nutrientsMultiply(dish.getNutrients100grams(), weight / 100d);
			wf.setNutrientsTotal(nutrientsTotal);
			
			consumedDishList.add(wf);
		}
		
		return consumedDishList;
	}

	public void addConsumedProduct(ConsumedProduct cp)
	{
		this.consumedProductList.add(cp);
	}

	public List<ConsumedProduct> getConsumedProductList()
	{
		return this.consumedProductList;
	}

	public void setConsumedProductList(Database db, List<ConsumedProduct> consumedProductList) throws DatabaseException
	{
		// eh... try jpa! :-)
		Query<Product> qp;
		;
		Query<Nutrients> qn;
		for (ConsumedProduct cp : consumedProductList)
		{
			// set its product
			qp = db.query(Product.class);
			qp.eq("id", cp.getProduct_Id());
			cp.setProduct(qp.find().get(0));

			// set its nutrients
			qn = db.query(Nutrients.class);
			qn.eq("id", cp.getNutrients_Id());
			cp.setNutrients(qn.find().get(0));
		}
		this.consumedProductList = consumedProductList;
	}

	public boolean anyConsumedProduct()
	{
		int n = 0;
		for (ConsumedProduct cp : this.getConsumedProductList())
			if (!cp.getRemoved())
				n++;

		return (0 < n);
	}

	public String getVitaminePlotPath()
	{
		return this.vitamine.getWebPath();
	}

	public String getVitaminePlusPlotPath()
	{
		return this.vitaminePlus.getWebPath();
	}
	
	public boolean isDirtyVitaminPlots()
	{
		return this.dirtyVitaminPlots;
	}

	public String getGender()
	{
		if (isLoggedIn())
		{
			return lmdUser.getGender();
		} else
		{
			return GENDER_DEFAULT;
		}
	}

	public String getWebGender()
	{
		String g = getGender();
		if (GENDER_MALE.equals(g))
			return GENDER_MALE_DUTCH;
		return GENDER_FEMALE_DUTCH;
	}

	public String getAge()
	{
		if (isLoggedIn() && null != lmdUser.getBornDate())
		{
			java.util.Date today = new Date();

			long msYear = 1000L * 60 * 60 * 24 * 365;
			long msDiff = today.getTime() - lmdUser.getBornDate().getTime();

			return String.valueOf(msDiff / msYear);

		} else
		{
			return AGE_DEFAULT;
		}
	}

	public void updateFiguresDB(Database db) throws DatabaseException
	{
		if (isLoggedIn())
		{
			figs.setDay(getFoodDateDate());
			figs.setLmdUser(getLmdUser());
			figs.setVitamin(vitamine.getWebPath());
			figs.setVitaminPlus(vitaminePlus.getWebPath());

			if (null == figs.getId())
				db.add(figs);
			else
				db.update(figs);
		}
	}

	public void removeFiguresDB(Database db) throws DatabaseException
	{
		if (isLoggedIn() && null != figs.getId())
		{
			db.remove(figs);
		}
	}

	public void setVitaminDayFigures(VitaminDayFigures vitaminDayFigures)
	{
		this.figs = vitaminDayFigures;
	}

	public String getMyDataBornDate()
	{
		if (this.isLoggedIn())
			return niceDate(this.getLmdUser().getBornDate(), DATE_DASHED);
		else
		{
			return DATE_DEFAULT;
		}
	}

	public String getMyDataDiabetesDate()
	{
		if (this.isLoggedIn())
			return niceDate(this.getLmdUser().getDiabetesDate(), DATE_DASHED);
		else
		{
			return "";
		}
	}

	public String getMyDataPumpDate()
	{
		if (this.isLoggedIn())
			return niceDate(this.getLmdUser().getPumpDate(), DATE_DASHED);
		else
		{
			return "";
		}
	}

	public String getMyDataSensorDate()
	{
		if (this.isLoggedIn())
			return niceDate(this.getLmdUser().getSensorDate(), DATE_DASHED);
		else
		{
			return "";
		}
	}

	public List<Question> getHelpItems()
	{
		return this.helpItems.questionList;
	}

	public String getScreenHelpTab()
	{
		return this.screenHelpTab;
	}
}
