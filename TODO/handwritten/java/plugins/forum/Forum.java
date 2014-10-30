package plugins.forum;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lmd.ConsumedProduct;
import lmd.Feedback;
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
import org.molgenis.framework.ui.EasyPluginController;
import org.molgenis.framework.ui.FreemarkerView;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenView;
import org.molgenis.util.RScript;
import org.molgenis.util.SimpleTuple;
import org.molgenis.util.Tuple;

import plugins.forum.ExternalData.ImportData;
import plugins.forum.Levenstein;
/**
 * GenericWizardController takes care of all user requests and application
 * logic.
 * 
 * <li>Each user request is handled by its own method based action=methodName.
 * <li>MOLGENIS takes care of db.commits and catches exceptions to show to the
 * user <li>GenericWizardModel holds application state and business logic on top
 * of domain model. Get it via this.getModel()/setModel(..) <li>
 * GenericWizardView holds the template to show the layout. Get/set it via
 * this.getView()/setView(..).
 */
public class Forum extends EasyPluginController<ForumModel>
{
	public Forum(String name, ScreenController<?> parent)
	{
		super(name, parent);
		this.setModel(new ForumModel(this)); // the default model
	}

	public ScreenView getView()
	{
		return new FreemarkerView(getModel().getScreen(), getModel());
	}

	/**
	 * At each page view: reload data from database into model and/or change.
	 * 
	 * Exceptions will be caught, logged and shown to the user automatically via
	 * setMessages(). All db actions are within one transaction.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void reload(Database db) throws Exception
	{
		// import data if no data in db
		if (emptyDb(db))
		{
			ImportData.main(db);

			// create account
			LmdUser u = new LmdUser();
			u.setForumName("MD");
			u.setFirstName("Martijn");
			u.setLastName("Dijkstra");
			u.setEmail("martijndijkstra1980@gmail.com");
			u.setPassword("wwww");
			u.setGender("male");
			u.setActiveAccount(true);
			u.setActivationCode("123");
			db.add(u);
		}

		// ---- REMOVE THIS ---->
		if (!getModel().isLoggedIn())
		{
			Tuple request = new SimpleTuple();
			request.set("email", "martijndijkstra1980@gmail.com");
			request.set("password", "wwww");
			this.login(db, request);
		}
		// <---- REMOVE THIS ----

//		if (getModel().isLoggedIn())
//		{
//			db.setLogin(getModel().getLogin());
//		}
		
		if (getModel().DISH_VIEW.equals(getModel().getScreen()))
		{
			if (getModel().isLoggedIn() && getModel().isScreenTabDishOverview())
			{
				if (!getModel().isLoggedIn())
				{
					getModel().dishList = new ArrayList<Food>();
				} else
				{
					// load dishes from db
					Query<Food> q = db.query(Food.class);
					q.eq("lmdUser", getModel().lmdUser.getId());
					q.eq("removed", false);
					getModel().dishList = q.find();

					// also load all nutrients
					Query<Nutrients> qn;
					for (Food d : getModel().dishList)
					{
						qn = db.query(Nutrients.class);
						qn.eq("id", d.getNutrients100grams_Id());
						d.setNutrients100grams(qn.find().get(0));
					}
				}
			}
		} else if (getModel().getScreen().equals(getModel().TOPIC_VIEW))
		{
			// set get all posts so that we can retrieve who posted last in each
			// topic
			// (ie we need the updated list when sorting topics)
			Query<Post> q2 = db.query(Post.class);
			getModel().setFullPostList((List<Post>) q2.find());

			Query<Topic> q = db.query(Topic.class);
			getModel().setTopicList(getModel().sortTopicList((List<Topic>) q.find()));

		} else if (getModel().getScreen().equals(getModel().MESSAGE_VIEW))
		{
			if (getModel().getCurrentTopic() == null)
			{
				// safety check: if in message view and no topic selected, then go to topicview
				getModel().setScreen(getModel().TOPIC_VIEW);
			} else
			{
				Query<Post> q = db.query(Post.class);
				q.eq("topic", getModel().getCurrentTopic().getId());
				getModel().setPostList((List<Post>) q.find());

				// get all users to set userinfo
				Query<LmdUser> q2 = db.query(LmdUser.class);
				getModel().setLmdUserMap((List<LmdUser>) q2.find());

				Query<Post> q3 = db.query(Post.class);
				getModel().setPostMap((List<Post>) q3.find());
			}
		}
	}

	private boolean emptyDb(Database db) throws DatabaseException
	{
		return db.find(Product.class).size() == 0;
	}

	public void toPreviousScreen(Database db, Tuple request)
	{
		getModel().toPreviousScreen();
	}

	public void toVoedingsdagboekView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().VOEDINGSDAGBOEK_VIEW);
	}

	public void toAddTopicView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().ADDTOPIC_VIEW);
	}

	public void toTopicView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().TOPIC_VIEW);
	}

	public void toDishView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().DISH_VIEW);

		// if tab is set, then goto that tab
		String tab = request.getString("tab");

		// not logged in? Enforce tab = overview
		if (!getModel().isLoggedIn())
			tab = "overview";

		if ("edit".equals(tab))
		{
			getModel().setScreenDishTab(getModel().DISH_VIEW_TAB_EDIT);

			Integer dishId = request.getInt("dishId");
			if (null == dishId)
			{
				// user wants to create a new dish
				getModel().currentDish = new Food();
				getModel().currentDish.setName("");

				getModel().currentDish.setLmdUser(getModel().getLmdUser().getId()); // immediately set user
				getModel().currentDishWeightedFood = new ArrayList<WeightedFood>();
				getModel().dishNutrients100grams = new Nutrients();
			} else
			{
				// user wants to edit a dish
				Iterator<Food> it = getModel().dishList.iterator();
				boolean found = false;
				while (it.hasNext() && !found)
				{
					getModel().currentDish = it.next();
					found = dishId.equals(getModel().currentDish.getId());
				}

				if (!found)
				{
					// st is not correct... goto main screen
					getModel().setScreen(getModel().VOEDINGSDAGBOEK_VIEW);
				} else
				{
					getModel().dishNutrients100grams = getModel().currentDish.getNutrients100grams();

					Query<WeightedFood> q = db.query(WeightedFood.class);
					q.eq("lmdUser", getModel().lmdUser.getId());
					q.eq("dish", dishId);
					getModel().currentDishWeightedFood = q.find();

					// for each weighted food, also load the food + nutrientsTotal
					Query<Food> qf;
					Query<Nutrients> qn;
					for (WeightedFood wf : getModel().currentDishWeightedFood)
					{
						qf = db.query(Food.class);
						qf.eq("id", wf.getFood_Id());
						wf.setFood(qf.find().get(0));

						qn = db.query(Nutrients.class);
						qn.eq("id", wf.getNutrientsTotal_Id());
						wf.setNutrientsTotal(qn.find().get(0));
					}
				}
			}
		} else if ("overview".equals(tab))
		{
			getModel().setScreenDishTab(getModel().DISH_VIEW_TAB_OVERVIEW);
		}
	}

	public void toFoodView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().FOOD_VIEW);
	}

	public void toDiabetesView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().DIABETES_VIEW);
	}

	public void toHelpView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().HELP_VIEW);
		getModel().setScreenTabHelp(request.getString("item"));
	}

	public void toMyDataView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().MY_DATA_VIEW);

		// prepare list with current forumNames to prevent duplicates
		Query<LmdUser> q = db.query(LmdUser.class);
		List<LmdUser> fuLst;
		fuLst = q.find();

		List<String> forumNameLst = new ArrayList<String>();
		for (LmdUser fu : fuLst)
		{
			// if loged in, exclude own forumName. You want to be able to keep
			// your own name when updating your info
			boolean loggedIn = getModel().isLoggedIn();
			if (!loggedIn || !fu.getForumName().equals(getModel().getForumName()))
			{
				forumNameLst.add("\"" + fu.getForumName() + "\"");
			}
		}

		getModel().setForumNameList(forumNameLst.toString());
	}

	public void toMessageView(Database db, Tuple request) throws Exception
	{
		getModel().setScreen(getModel().MESSAGE_VIEW);

		Integer id = request.getInt("topicId");

		// set current topic
		boolean done = false;
		Iterator<Topic> it = getModel().getTopicList().iterator();
		while (!done && it.hasNext())
		{
			Topic t = it.next();
			if (t.getId().equals(id))
			{
				done = true;
				getModel().setCurrentTopic(t);
			}
		}
	}

	public boolean isLoggedIn(Database db, Tuple request) throws Exception
	{
		return getModel().lmdUser != null;
	}

	public void login(Database db, Tuple request) throws Exception
	{
		Query<LmdUser> q = db.query(LmdUser.class);
		q.eq("email", request.getString("email"));
		q.eq("password", request.getString("password"));
		q.eq("activeAccount", true);
		List<LmdUser> fuLst = q.find();

		if (1 == fuLst.size())
		{
			// we found a match!
			getModel().lmdUser = fuLst.get(0);

			// set login class so that model and services can communicate
			SimpleLogin login = (SimpleLogin)this.getApplicationController().getLogin();
			login.setLmdUser(getModel().lmdUser);
			db.setLogin(login);

			// Remember current consumptions and prepend them after loading
			List<ConsumedProduct> cpLst = getModel().getConsumedProductList();

			if (0 < cpLst.size())
			{
				List<Nutrients> nutrLst = new ArrayList<Nutrients>();

				// update user id & get nutr
				Iterator<ConsumedProduct> it = cpLst.iterator();
				while (it.hasNext())
				{
					ConsumedProduct cp = it.next();
					cp.setLmdUser(getModel().getLmdUser());
					nutrLst.add(cp.getNutrients());
				}

				// store stuff in db: consumption + nutr
				db.add(nutrLst);
				db.add(cpLst);
			}

			// load consumptions in model
			loadConsumptions(db);

			// load cache
			loadVitaminDayFigures(db);

			// make figs dirty if already consumptions were filled in
			if (0 < cpLst.size())
				setFoodFigsDirtyState(db);

			// set screen if still on front page
			if (getModel().isVoedingsdagboekView())
			{
				getModel().setScreen(getModel().FOOD_VIEW);
				getModel().setScreenFoodTab(getModel().FOOD_VIEW_TAB_FOOD);
			}
			
			// set success alert!
			getModel().getAlert().clear();
			getModel().setAlert(getModel().getAlert().LOGIN_SUCCESS);
		} else
		{
			q = db.query(LmdUser.class);
			q.eq("email", request.getString("email"));
			q.eq("activeAccount", false);
			fuLst = q.find();

			if (1 == fuLst.size())
			{
				getModel().setAlert(getModel().getAlert().ACCOUNT_NOT_ACTIVATED);
			} else
			{
				getModel().setAlert(getModel().getAlert().LOGIN_FAILED);
			}
		}
	}

	private void loadVitaminDayFigures(Database db) throws DatabaseException
	{
		Query<VitaminDayFigures> qfigs = db.query(VitaminDayFigures.class);
		qfigs.eq("lmdUser", getModel().lmdUser.getId());

		// load only consumptions current date
		qfigs.eq("day", getModel().getFoodDateDB());

		List<VitaminDayFigures> lvdf = qfigs.find();

		if (0 == lvdf.size() || 1 < lvdf.size())
		{ // TODO: catch: 1 < lvdf.size(), should not happen
			// renew db-object
			getModel().setVitaminDayFigures(new VitaminDayFigures());
			// dirty?
			setFoodFigsDirtyState(db);
		} else
		{
			// found 1
			VitaminDayFigures vdf = lvdf.get(0);
			getModel().vitamine.setWebPath(vdf.getVitamin());
			getModel().vitaminePlus.setWebPath(vdf.getVitaminPlus());
			// renew db-object
			getModel().setVitaminDayFigures(vdf);

			// figs are not dirty
			getModel().dirtyVitaminPlots = false;
		}
	}

	private void loadConsumptions(Database db) throws DatabaseException
	{
		Query<ConsumedProduct> qcp = db.query(ConsumedProduct.class);
		qcp.eq("lmdUser", getModel().lmdUser.getId());
		// load only consumptions current date
		qcp.eq("consumptionDate", getModel().getFoodDateDB());

		getModel().setConsumedProductList(db, qcp.find());
	}

	public void logout(Database db, Tuple request) throws Exception
	{
		getModel().lmdUser = null;
		
		// set login class so that model and services can communicate
		SimpleLogin login = (SimpleLogin)this.getApplicationController().getLogin();
		login.setLmdUser(getModel().lmdUser);
		db.setLogin(login);
		
		getModel().setScreen(getModel().VOEDINGSDAGBOEK_VIEW);
	}

	public void activate(Database db, Tuple request) throws Exception
	{
		// only non-active accounts can be activated, log in is for free, first
		// time
		Query<LmdUser> q = db.query(LmdUser.class);
		q.eq("activationCode", request.getString("activationCode"));
		List<LmdUser> fuLst = q.find();

		if (1 == fuLst.size())
		{
			// we found a match!
			LmdUser fu = fuLst.get(0);

			// if not active, then activate and login
			if (!fu.getActiveAccount())
			{
				// log in
				getModel().lmdUser = fu;
				// activate account
				getModel().lmdUser.setActiveAccount(true);
				db.update(getModel().lmdUser);
			} else
			{
				// display message that user did not activate
				getModel().getAlert().setMessageType(getModel().getAlert().ACCOUNT_ALREADY_ACTIVATED);
			}
		}
	}

	/**
	 * Year month day as java.util.Date
	 * 
	 * @param ddmmyyyy
	 *            ~ dd-mm-yyyy
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public java.util.Date string2date(String ddmmyyyy, boolean ignoreDay)
	{
		if (ddmmyyyy == null || ddmmyyyy.length() != 10)
			return null;

		Integer day = (ignoreDay ? 1 : Integer.parseInt(ddmmyyyy.substring(0, 2)));
		Integer month = Integer.parseInt(ddmmyyyy.substring(3, 5));
		Integer year = Integer.parseInt(ddmmyyyy.substring(6, 10));
		return new java.util.Date(year - 1900, month - 1, day);
	}

	/**
	 * Send password to given email
	 * 
	 * @param email
	 * @return
	 */
	private boolean emailPassword(LmdUser fu)
	{
		Small s = new Small();
		return s.sendActivationMail(fu);
	}

	public void resendPassword(Database db, Tuple request) throws Exception
	{
		// check whether email already exists!
		Query<LmdUser> q = db.query(LmdUser.class);
		q.eq("email", request.getString("email"));
		List<LmdUser> fuList = q.find();
		if (1 == fuList.size())
		{
			LmdUser fu = fuList.get(0);
			// email exists already, resend activation link + email / password
			if (emailPassword(fu))
			{
				// deactivate account
				fu.setActiveAccount(false);
				db.update(fu);

				getModel().getAlert().setMessageType(getModel().getAlert().RESEND_PASSWORD_SUCCESS);
			} else
			{
				getModel().getAlert().setMessageType(getModel().getAlert().EMAIL_RESEND_FAILURE);
			}

			getModel().setScreen(getModel().TOPIC_VIEW);
		} else
		{
			getModel().getAlert().setMessageType(getModel().getAlert().EMAIL_RESEND_FAILURE);
		}
	}

	public void updateAccount(Database db, Tuple request) throws Exception
	{
		LmdUser fu = new LmdUser();

		if (getModel().isLoggedIn())
		{ // user is loged in and wants to update his/her info
			fu = getModel().getLmdUser();
		} else
		{ // new user, new account
			// check whether email already exists!
			Query<LmdUser> q = db.query(LmdUser.class);
			q.eq("email", request.getString("email"));
			List<LmdUser> fuList = q.find();
			if (1 == fuList.size())
			{
				LmdUser existingFu = fuList.get(0);
				// email exists already, resend activation link
				if (emailPassword(existingFu))
				{
					// deactivate account
					existingFu.setActiveAccount(false);
					db.update(existingFu);

					getModel().getAlert().setMessageType(getModel().getAlert().EMAIL_EXISTS);
				} else
				{
					getModel().getAlert().setMessageType(getModel().getAlert().EMAIL_RESEND_FAILURE);
				}

				getModel().setScreen(getModel().FOOD_VIEW);
				return;
			}
		}

		// else continue and create new user
		fu.setFirstName(request.getString("firstName"));
		fu.setLastName(request.getString("lastName"));
		if (!getModel().isLoggedIn()) // email cannot be changed
			fu.setEmail(request.getString("email"));
		fu.setPassword(request.getString("password"));
		fu.setSensorDate(string2date(request.getString("sensorDate"), false));
		fu.setPumpDate(string2date(request.getString("pumpDate"), false));
		fu.setDiabetesDate(string2date(request.getString("diabetesDate"), false));
		fu.setBornDate(string2date(request.getString("bornDate"), false));
		String gender = request.getString("gender");
		if (null == gender)
			gender = "female";
		fu.setGender(gender);
		fu.setForumName(request.getString("forumName"));

		if (getModel().isLoggedIn())
		{ // update user info
			db.update(fu);

			getModel().getAlert().setMessageType(getModel().getAlert().UPDATE_ACCOUNT_SUCCESS);
		} else
		{ // set creation date and send activation email (only for new account)
			// creation date
			Date today = new java.util.Date();
			fu.setCreated(today);

			// activation code
			String code = "No-activation-code!";

			// prevent double acitvation codes in db
			boolean unique = false;
			Query<LmdUser> qfu = db.query(LmdUser.class);

			while (!unique)
			{
				code = Integer.toString(99999 + (int) (Math.random() * ((Integer.MAX_VALUE - 99999) + 1)));
				qfu.eq("activationCode", code);
				unique = qfu.find().size() == 0;
			}

			fu.setActivationCode(code);
			Small s = new Small();
			if (s.sendActivationMail(fu))
			{
				db.add(fu);
				getModel().getAlert().setMessageType(getModel().getAlert().REGISTRATION_SUCCESS);
			} else
			{
				getModel().getAlert().setMessageType(getModel().getAlert().EMAIL_FAILED);
			}

			// goto topic view
			getModel().setScreen(getModel().TOPIC_VIEW);
		}
	}

	public void feedback(Database db, Tuple request) throws Exception
	{
		getModel().getAlert().setMessageType(getModel().getAlert().FEEDBACK_FAILED);

		String subject = request.getString("subject");
		String message = request.getString("message");
		String email = request.getString("email");

		if (null == request.getString("verificationCode") || !"8".equals(request.getString("verificationCode").trim()))
		{
			getModel().getAlert().setMessageType(getModel().getAlert().FEEDBACK_FAILED);
			return;
		}

		if (null == email && getModel().isLoggedIn())
			email = getModel().getLmdUser().getEmail();

		Feedback f = new Feedback();
		f.setSubject(subject);
		f.setMessage(message);
		f.setEmail(email);
		if (getModel().isLoggedIn())
			f.setLmdUser(getModel().getLmdUser());

		// add to db
		db.add(f);

		// send mail
		message = "Feedback van (e-mail): " + email + "\n\n" + message;

		message = message.replaceAll("\n", "<BR/>");

		if (new Small().sendFeedback(subject, message))
			getModel().getAlert().setMessageType(getModel().getAlert().FEEDBACK_SUCCESS);
	}

	public void saveNewTopic(Database db, Tuple request) throws Exception
	{
		// save topic
		Topic t = new Topic();
		t.setTitle(request.getString("title"));
		t.setLmdUser(getModel().lmdUser);

		// First check whether topic exists already:
		if (topicExists(t))
		{
			getModel().getAlert().setMessageType(getModel().getAlert().NEW_TOPIC_DUPLICATE);
			return;
		}

		// else add and continue
		db.add(t);

		// set current topic, so that we can also save first post
		getModel().setCurrentTopic(t);

		if (null != request.getString("message"))
		{
			saveNewPost(db, request);
		}

		// goto topic view
		getModel().getAlert().setMessageType(getModel().getAlert().NEW_TOPIC_SUCCESS);
		getModel().setScreen(getModel().TOPIC_VIEW);
	}

	private boolean topicExists(Topic newTopic)
	{
		for (Topic t : getModel().getTopicList())
		{
			if (t.getTitle().equalsIgnoreCase(newTopic.getTitle()))
				return true;
		}

		return false;
	}

	public void saveNewPost(Database db, Tuple request) throws Exception
	{
		Post p = new Post();
		p.setTopic(getModel().getCurrentTopic());
		p.setLmdUser(getModel().lmdUser);

		String message = request.getString("message");
		// handle citations
		if (getModel().isCitation())
		{
			final Pattern pattern = Pattern.compile("(.*)\\[begin](.*)\\[eind](.*)", Pattern.DOTALL);
			final Matcher matcher = pattern.matcher(message);

			if (matcher.find())
			{
				// we deal indeed with a citation
				// escape the stuff
				String before = escapeHtml(matcher.group(1));
				String citation = escapeHtml(matcher.group(2));
				String after = escapeHtml(matcher.group(3));

				Post cp = getModel().getCitePost();

				message = before + "<BLOCKQUOTE>" + citation + "<A HREF=\"#" + cp.getId() + "\" CLASS=\"goto\"><SMALL>" + "Geschreven door " + cp.getLmdUser_ForumName() + ", op <CITE>" + getModel().niceDate(cp.getCreated(), getModel().DATE_FULL) + "</CITE></SMALL></A></BLOCKQUOTE>" + after;

				// alse set appropriate xref
				p.setCitedPost(cp.getId());
			}
		} else
		{
			// escape message
			message = escapeHtml(message);
		}

		p.setMessage(message);

		// add new post to db
		db.add(p);

		// also update #posts current topic
		Query<Post> q = db.query(Post.class);
		q.eq("topic", getModel().getCurrentTopic().getId());
		List<Post> postList = q.find();
		getModel().getCurrentTopic().setNPosts(postList.size() - 1);

		// and update current topic in db
		db.update(getModel().getCurrentTopic());

		// reset citation to none
		getModel().setCitePostId(null);
	}

	public void eraseMessage(Database db, Tuple request) throws Exception
	{
		getModel().setCitePostId(null);
		getModel().setEditPostId(null);
	}

	public void citePost(Database db, Tuple request) throws Exception
	{
		getModel().setCitePostId(request.getInt("postId"));
		getModel().setEditPostId(null); // never at same time
	}

	public void editPost(Database db, Tuple request) throws Exception
	{
		getModel().setEditPostId(request.getInt("postId"));
		getModel().setCitePostId(null); // never at same time
	}

	public void editPostMessage(Database db, Tuple request) throws Exception
	{
		Post editedPost = getModel().getPost(getModel().getEditPostId());

		// set date of edit
		editedPost.setChangeDate(new java.util.Date());

		// set author of edit
		editedPost.setChangedBy(getModel().getLmdUser());

		// set percentage
		String original = editedPost.getMessage();
		String edited = request.getString("message");

		Integer originalPerc = editedPost.getChangePercentage();
		if (null == originalPerc)
			originalPerc = 0;

		Integer editedPerc = Levenstein.getLevenshteinPercentage(original, edited);
		editedPost.setChangePercentage(Math.min(100, originalPerc + editedPerc));

		// set reason
		String reason = request.getString("reason");
		reason = escapeHtml(null == reason ? "" : reason);
		editedPost.setChangeReason(reason);

		// set new message
		// vervang [begin] [eind] weer door <BLOCKQ>...
		// handle citations
		if (null != editedPost.getCitedPost_Id())
		{
			final Pattern pattern = Pattern.compile("(.*)\\[begin](.*)\\[eind](.*)", Pattern.DOTALL);
			final Matcher matcher = pattern.matcher(edited);

			if (matcher.find())
			{
				// we deal indeed with a citation
				// escape the stuff
				String before = escapeHtml(matcher.group(1));
				String citation = escapeHtml(matcher.group(2));
				String after = escapeHtml(matcher.group(3));

				Post cp = getModel().getPost(editedPost.getCitedPost_Id());

				edited = before + "<BLOCKQUOTE>" + citation + "<A HREF=\"#" + cp.getId() + "\" CLASS=\"goto\"><SMALL>" + "Geschreven door " + cp.getLmdUser_ForumName() + ", op <CITE>" + getModel().niceDate(cp.getCreated(), getModel().DATE_FULL) + "</CITE></SMALL></A></BLOCKQUOTE>" + after;
			}
		} else
		{
			// escape message
			edited = escapeHtml(edited);
		}

		editedPost.setMessage(edited);

		// update db
		db.update(editedPost);

		// done editing
		getModel().setEditPostId(null);
	}

	public void gotoDate(Database db, Tuple request) throws Exception
	{
		getModel().setFoodDate(string2date(request.getString("date"), false));

		loadConsumptions(db); // load consumptions into model, given new date

		// load cache
		loadVitaminDayFigures(db);

		// if we are in Vitamin/Overzicht tab, then also calculate figs if we'd
		// eaten st and fig is not in cache yet:
		if (getModel().isScreenTabFoodVitamin() && getModel().dirtyVitaminPlots && getModel().anyConsumedProduct())
		{
			this.createVitaminePlot(db);
		}

		// just to be sure that screen is food:
		getModel().setScreen(getModel().FOOD_VIEW);
	}

	/**
	 * TODO: make code easier which is now possible because we don't have to
	 * handle the edits/updates here anymore
	 */
	public void saveFood(Database db, Tuple request) throws Exception
	{
		List<Product> prodLst = new ArrayList<Product>();
		ConsumedProduct cp = new ConsumedProduct();
		Nutrients nutrObject = new Nutrients();

		String choice = request.getString("foodInput");
		if (null == choice)
			choice = "";
		else
			choice = choice.trim();

		// if no food, check if we want to update amount of some product
		boolean editAmountOfConsumption = 0 == choice.length();
		if (editAmountOfConsumption)
		{
			// store in this list the products that were marked for 'removal'
			List<ConsumedProduct> selection = getSelectedConsumptions(request);

			// if only one product was selected, then update the amount of that
			// product
			if (0 == selection.size())
			{// do nothing (no food, no selection)
				return;
			} else if (1 == selection.size())
			{
				// set the consumed product we want to edit
				cp = selection.get(0);

				// set selected product (for processing below)
				prodLst = new ArrayList<Product>(Arrays.asList(cp.getProduct()));

				// also get the nutr of this
				nutrObject = cp.getNutrients();
			} else
			{// user has selected multiple ConsumedProducts, thus ignore request
				getModel().setAlert(getModel().getAlert().AMOUNT_CHANGED_ERROR_SELECTION_TOO_BIG);
				return;
			}
		} else
		{
			// else get product from db
			Query<Product> q = db.query(Product.class);
			q.eq("Naam", choice);
			prodLst = q.find();
		}

		if (0 == prodLst.size())
		{ // error product not found
			getModel().setAlert(getModel().getAlert().PRODUCT_UNKNOWN);
		} else if (1 < prodLst.size())
		{ // should not happen
			// multiple products with same name?
			getModel().setAlert(getModel().getAlert().ERROR_MULTIPLE_PRODUCTS_SAME_NAME);
		} else
		{ // found 1 match (expected)
			Product p = prodLst.get(0);

			if (getModel().isLoggedIn())
				cp.setLmdUser(getModel().getLmdUser());
			cp.setProduct(p);
			Double weight = getWeight(p, request, 1d);
			cp.setWeight(weight);
			Nutrients nut = getNutrients(nutrObject, p, cp.getWeight());
			cp.setNutrients(nut);
			cp.setConsumptionDate(getModel().getFoodDateDate());

			// if logged in, then store item in db
			if (null != cp.getLmdUser())
			{
				if (editAmountOfConsumption)
				{// update stuff
					db.update(nut);
					db.update(cp);
				} else
				{// add stuff
					db.add(nut);
					db.add(cp);
				}
			}

			// so far so good, lets add product to model if we were not editing
			if (!editAmountOfConsumption)
				getModel().addConsumedProduct(cp);

			// make figs dirty
			setFoodFigsDirtyState(db);

			// set alert
			if (editAmountOfConsumption)
				getModel().setAlert(getModel().getAlert().AMOUNT_CHANGED);
			else
				getModel().setAlert(getModel().getAlert().CONSUMPTION_ADDED);
		}

	}

	/**
	 * @deprecated You should use same function with Food instead of Product
	 **/
	private Nutrients getNutrients(Nutrients nut, Product p, Double weight)
	{
		for (String field : nut.getFields(true))
		{
			try
			{
				nut.set(field, weight / 100 * (Double) p.get(field));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return nut;
	}

	/** rename to saveFood */
	public void saveDish(Database db, Tuple request) throws Exception
	{
		String foodType = request.getString("foodType");
		boolean isConsumption = getModel().FOOD_TYPE_CONSUMPTION.equals(foodType);
		boolean isDish = getModel().FOOD_TYPE_DISH.equals(foodType);

		if ((!getModel().isLoggedIn() && isDish) || (!isConsumption && !isDish))
		{ // we should not get here! Reset screen
			getModel().setScreen(getModel().VOEDINGSDAGBOEK_VIEW);
			return;
		}

		// map vars from either consumption or dish
		Food currentDish = null;
		List<WeightedFood> weightedFoodList = null;

		// create local parameters
		if (isConsumption)
		{
			weightedFoodList = getModel().getCurrentConsumptionList();
		} else if (isDish)
		{
			currentDish = getModel().currentDish;
			weightedFoodList = getModel().currentDishWeightedFood;
		}

		if (isDish)
		{
			// update fields of currentDish
			currentDish.setDishCategory(request.getString("course"));
			currentDish.setUnit("portie");

			// also set name
			String name = request.getString("dishName");
			if (null == name || "".equals(name))
				name = "Naam gerecht ontbreekt";
			currentDish.setName(name);

			// ensure that currentDish has an id so that we can also add its weightedFood which refers to it
			// food -> nutrients obligatory, so we may have to add nutrients first
			if (null == currentDish.getId())
			{
				if (null == currentDish.getNutrients100grams_Id())
				{
					Nutrients n = newNutrients();
					db.add(n);
					currentDish.setNutrients100grams(n);
				}
				db.add(currentDish);
			}
		}
		// user wants to add 0 or 1 weighted foods (incl. add to db!)
		// added == true if no food found and/or added
		// dish is ignored if null == dish (consumedProduct assumed)
		addWeightedFoodToDish(db, currentDish, weightedFoodList, request);

		if (isDish)
		{
			// update dish description + nutrients100grams + unit weight
			updateDish(db, currentDish);
		}
	}

	/**
	 * Update dish weight, dish description (serving = ... grams) and dish
	 * nutrients/100grams Also add/update in db
	 * 
	 * @param db
	 * @param dish
	 * @throws DatabaseException
	 */
	private void updateDish(Database db, Food dish) throws DatabaseException
	{
		// iterate through weightedFoods to figure out weight + nutrients
		// per 100g for this dish
		Double weight = 0d;
		Nutrients nutrientsTotal = newNutrients(dish.getNutrients100grams_Id());

		Query<WeightedFood> q = db.query(WeightedFood.class);
		q.eq("dish", dish.getId());
		List<WeightedFood> weightedFoodList = q.find();

		Iterator<WeightedFood> it = weightedFoodList.iterator();
		while (it.hasNext())
		{
			WeightedFood dishComponent = it.next();

			Query<Nutrients> qn = db.query(Nutrients.class);
			qn.eq("id", dishComponent.getNutrientsTotal_Id());
			Nutrients n = qn.find().get(0);

			weight += dishComponent.getWeight();

			nutrientsTotal = nutrientsAdd(nutrientsTotal, n);
		}

		Nutrients nutrients100grams = nutrientsMultiply(nutrientsTotal, 100d / weight);

		// set weight + nutr/100g + descriptive name
		dish.setUnitWeight(weight);
		dish.setNutrients100grams(nutrients100grams);
		dish.setNameDescriptive(dish.getName() + " (" + dish.getUnit() + " = " + Math.round(dish.getUnitWeight()) + " gram)");

		// also add/update in db
		if (null == nutrients100grams.getId())
			db.add(nutrients100grams);
		else
			db.update(nutrients100grams);

		if (null == dish.getId())
			db.add(dish);
		else
			db.update(dish);
	}

	/**
	 * Retrieve weightedFood (wf) from request and add to weightedFoodList Also
	 * save wf
	 * 
	 * @param db
	 * @param dish
	 * @param weightedFoodList
	 * @param request
	 * @return whether we added a weighted food
	 * @throws DatabaseException
	 */
	private boolean addWeightedFoodToDish(Database db, Food dish, List<WeightedFood> weightedFoodList, Tuple request) throws DatabaseException
	{
		Food f = getFoodFromRequest(db, request);

		if (null == f)
			return false; // no matching food found

		Double weight = getWeight(f, request, 1d);

		Nutrients nutrientsTotal = nutrientsMultiply(f.getNutrients100grams(), weight / 100d);
		nutrientsTotal.setId(null); // otherwise we would overwrite the nutr of
									// f!

		WeightedFood wf = new WeightedFood();
		if (getModel().isLoggedIn())
			wf.setLmdUser(getModel().getLmdUser());

		if (null != dish)
		{ // we deal with a dish
			wf.setDish(dish); // the dish component belongs to this dish
		}
		wf.setFood(f);
		wf.setWeight(weight);
		wf.setNutrientsTotal(nutrientsTotal);

		weightedFoodList.add(wf);

		// also store in db if logged in!
		if (getModel().isLoggedIn())
		{
			db.add(nutrientsTotal);
			wf.setNutrientsTotal_Id(nutrientsTotal.getId());
			db.add(wf);
		}

		if (null != dish)
		{
			// set alert that we added nutrient
			getModel().setAlert(getModel().getAlert().INGREDIENT_ADDED);
		} else
		{
			getModel().setAlert(getModel().getAlert().CONSUMPTION_ADDED);
		}

		return true;
	}

	private Food getFoodFromRequest(Database db, Tuple request) throws DatabaseException
	{
		String choice = request.getString("foodInput");
		if (null == choice)
			return null;
		else
			choice = choice.trim();

		if (0 == choice.length())
			return null;

		// else get product from db
		Query<Food> q = db.query(Food.class);
		q.eq("nameDescriptive", choice);
		List<Food> foodCandidateList = q.find();

		if (0 == foodCandidateList.size())
		{ // error product not found
			getModel().setAlert(getModel().getAlert().PRODUCT_UNKNOWN);
		} else if (1 < foodCandidateList.size())
		{ // should not happen
			// multiple products with same name?
			getModel().setAlert(getModel().getAlert().ERROR_MULTIPLE_PRODUCTS_SAME_NAME);
		} else
		{ // found 1 match (expected)
			// now also get its nutrients and save in object
			Food f = foodCandidateList.get(0);
			Query<Nutrients> qn = db.query(Nutrients.class);
			qn.eq("id", f.getNutrients100grams_Id());
			Nutrients nut = qn.find().get(0);
			f.setNutrients100grams(nut);

			return f;
		}

		return null;
	}

	/** Used to init a new nutrients object */
	public Nutrients newNutrients()
	{
		Nutrients n = new Nutrients();

		for (String field : n.getFields(true))
		{
			try
			{
				n.set(field, (Double) 0d);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return n;
	}

	public Nutrients newNutrients(Integer id)
	{
		Nutrients n = newNutrients();
		if (null != id)
			n.setId(id);
		return n;
	}

	/** return gets id of first arg */
	private Nutrients nutrientsAdd(Nutrients nutWithID, Nutrients nut2)
	{
		Nutrients n = newNutrients(nutWithID.getId());

		for (String field : nutWithID.getFields(true))
		{
			try
			{
				n.set(field, (Double) nutWithID.get(field) + (Double) nut2.get(field));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return n;
	}

	/** return gets id of first arg (also used by model) */
	Nutrients nutrientsMultiply(Nutrients nut, Double factor)
	{
		if (factor.isInfinite())
			return nut;

		Nutrients n = newNutrients(nut.getId());

		for (String field : nut.getFields(true))
		{
			try
			{
				n.set(field, factor * (Double) nut.get(field));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return n;
	}

	private Double asDouble(String s)
	{
		if (null == s)
			return null;

		s = s.replace(',', '.');
		s = s.replaceAll("[^\\d.]", "");

		if ("".equals(s))
			return null;

		return Double.parseDouble(s);
	}

	/**
	 * 
	 * @param p
	 * @param request
	 * @param defaultServings
	 *            : if null, then return null
	 * @return
	 * @deprecated you should use Food, no longer Products
	 */
	@Deprecated
	private Double getWeight(Product p, Tuple request, Double defaultServings)
	{
		Double weight = asDouble(request.getString(getModel().constants.WEIGHT));
		Double units = asDouble(request.getString(getModel().constants.SERVINGS));
		if (null != weight)
		{
			return weight;
		} else
		{
			if (null == units)
				units = defaultServings;

			if (null == units)
				return null;

			return units * p.getEenheidGewicht();
		}
	}

	/**
	 * 
	 * @param p
	 * @param request
	 * @param defaultServings
	 *            : if null, then return null
	 * @return
	 */
	private Double getWeight(Food f, Tuple request, Double defaultServings)
	{
		Double weight = asDouble(request.getString(getModel().constants.WEIGHT));
		Double units = asDouble(request.getString(getModel().constants.SERVINGS));
		if (null != weight)
		{
			return weight;
		} else
		{
			if (null == units)
				units = defaultServings;

			if (null == units)
				return null;

			return units * f.getUnitWeight();
		}
	}

	public void deleteDish(Database db, Tuple request) throws Exception
	{
		Integer dishId = request.getInt("dishId");
		if (null != dishId && dishId.equals(getModel().currentDish.getId()))
		{
			getModel().currentDish.setRemoved(true);
			db.update(getModel().currentDish);

			// remove from memory
			Iterator<Food> it = getModel().dishList.iterator();
			while (it.hasNext())
				if (it.next().equals(getModel().currentDish))
					it.remove();

			getModel().currentDish = null;
		}

		getModel().setScreen(getModel().DISH_VIEW);
		getModel().setScreenDishTab(getModel().DISH_VIEW_TAB_OVERVIEW);
		getModel().setAlert(getModel().getAlert().DISH_REMOVED);
	}

	public void deleteWeightedFood(Database db, Tuple request) throws Exception
	{
		getModel().setAlert(getModel().getAlert().REMOVE_WEIGHTED_FOOD_FAILED);

		String foodType = request.getString("foodType");
		boolean isConsumption = getModel().FOOD_TYPE_CONSUMPTION.equals(foodType);
		boolean isDish = getModel().FOOD_TYPE_DISH.equals(foodType);

		if ((!getModel().isLoggedIn() && isDish) || (!isConsumption && !isDish))
		{ // we should not get here! Reset screen
			getModel().setScreen(getModel().VOEDINGSDAGBOEK_VIEW);
			return;
		}

		// map vars from either consumption or dish
		List<WeightedFood> weightedFoodList = null;

		// create local parameters
		if (isConsumption)
		{
			weightedFoodList = getModel().getCurrentConsumptionList();
		} else if (isDish)
		{
			weightedFoodList = getModel().currentDishWeightedFood;
		}

		// return if nothing to delete
		Integer id = request.getInt("id");
		if (null == id)
			return;

		Iterator<WeightedFood> it = weightedFoodList.iterator();
		WeightedFood wf = null;
		boolean found = false;
		while (it.hasNext() && !found)
		{
			wf = it.next();
			found = id.equals(wf.getId());
		}

		if (found)
		{
			db.remove(wf); // remove from db
			it.remove(); // remove from memory

			if (isConsumption)
			{
				getModel().setAlert(getModel().getAlert().CONSUMPTIONS_REMOVED);
			} else if (isDish)
			{
				// also update current dish weight and its nutrients
				updateDish(db, getModel().currentDish);
				getModel().setAlert(getModel().getAlert().INGREDIENT_REMOVED);
			}

		}
	}

	public void deleteConsumption(Database db, Tuple request) throws Exception
	{
		Integer cid = request.getInt("cid");
		if (null == cid)
			return;

		ConsumedProduct cp = getModel().getConsumedProductList().get(cid);
		cp.setRemoved(true);

		if (getModel().isLoggedIn())
		{
			db.update(cp);
		}

		// so far so good; make plots dirty
		this.setFoodFigsDirtyState(db);

		// and set alert
		getModel().setAlert(getModel().getAlert().CONSUMPTIONS_REMOVED);
	}

	public void updateWeightedFood(Database db, Tuple request) throws Exception
	{
		Integer id = request.getInt("id");
		if (null == id)
			return;

		String foodType = request.getString("foodType");
		boolean isConsumption = getModel().FOOD_TYPE_CONSUMPTION.equals(foodType);
		boolean isDish = getModel().FOOD_TYPE_DISH.equals(foodType);

		// map vars from either consumption or dish
		List<WeightedFood> weightedFoodList = null;

		// create local parameters
		if (isConsumption)
		{
			weightedFoodList = getModel().getCurrentConsumptionList();
		} else if (isDish)
		{
			weightedFoodList = getModel().currentDishWeightedFood;
		}

		WeightedFood wf = null;
		WeightedFood wfTest = null;
		Iterator<WeightedFood> it = weightedFoodList.iterator();
		while (it.hasNext())
		{
			wfTest = it.next();
			if (id.equals(wfTest.getId()))
				wf = wfTest;
		}

		if (null == wf)
		{
			getModel().setAlert(getModel().getAlert().WEIGHTED_FOOD_AMOUNT_UPDATE_FAILED);
		} else
		{
			// also get the refered food from db
			Query<Food> qf = db.query(Food.class);
			qf.eq("id", wf.getFood_Id());
			Food f = qf.find().get(0);

			// idem for nutr
			Query<Nutrients> qn = db.query(Nutrients.class);
			qn.eq("id", f.getNutrients100grams_Id());
			Nutrients foodNutrients100grams = qn.find().get(0);

			Double weight = getWeight(f, request, null);
			if (null == weight)
			{
				getModel().setAlert(getModel().getAlert().CONSUMPTION_UPDATED_NO_AMOUNT_ERROR);
			} else
			{
				if (weight.equals(wf.getWeight()))
				{
					getModel().setAlert(getModel().getAlert().CONSUMPTION_NOT_UPDATED_EQUAL_AMOUNT_WARNING);
				} else
				{
					wf.setWeight(weight);
					Nutrients nut = nutrientsMultiply(foodNutrients100grams, weight / 100d);
					nut.setId(wf.getNutrientsTotal_Id());

					wf.setNutrientsTotal(nut);

					// if logged in, then update item in db
					if (getModel().isLoggedIn())
					{
						// update stuff
						db.update(nut);
						db.update(wf);
					}

					if (isDish)
					{
						// also update dish
						updateDish(db, getModel().currentDish);
						db.update(getModel().currentDish);
					}

					// make new fig
					if (isConsumption)
						setFoodFigsDirtyState(db);

					getModel().setAlert(getModel().getAlert().WEIGHTED_FOOD_AMOUNT_UPDATED);
				}
			}
		}
	}

	public void updateConsumption(Database db, Tuple request) throws Exception
	{
		Integer cid = request.getInt("cid");
		if (null == cid)
			return;

		ConsumedProduct cp = getModel().getConsumedProductList().get(cid);

		Double weight = getWeight(cp.getProduct(), request, null);
		if (null == weight)
		{
			getModel().setAlert(getModel().getAlert().CONSUMPTION_UPDATED_NO_AMOUNT_ERROR);
		} else
		{
			if (weight.equals(cp.getWeight()))
			{
				getModel().setAlert(getModel().getAlert().CONSUMPTION_NOT_UPDATED_EQUAL_AMOUNT_WARNING);
			} else
			{
				cp.setWeight(weight);
				Nutrients nut = getNutrients(cp.getNutrients(), cp.getProduct(), weight);
				cp.setNutrients(nut);

				// if logged in, then update item in db
				if (null != cp.getLmdUser_Id())
				{
					// update stuff
					db.update(nut);
					db.update(cp);
				}

				// make new fig
				setFoodFigsDirtyState(db);

				getModel().setAlert(getModel().getAlert().AMOUNT_CHANGED);
			}
		}
	}

	/**
	 * Get list of food that was selected for removal, or to adapt amount of one
	 * consumption
	 * 
	 * @param request
	 * @return
	 */
	private List<ConsumedProduct> getSelectedConsumptions(Tuple request)
	{
		List<ConsumedProduct> selection = new ArrayList<ConsumedProduct>();
		for (int i = getModel().getConsumedProductList().size() - 1; 0 <= i; i--)
		{
			if (null != request.getString("remove" + i))
			{
				selection.add(getModel().getConsumedProductList().get(i));
			}
		}
		return selection;
	}

	private void createVitaminePlot(Database db) throws IOException, DatabaseException
	{
		// get fresh paths for new plot
		getModel().vitamine.refreshPath();
		getModel().vitaminePlus.refreshPath();

		RScript script = new RScript();
		script.R_COMMAND = "R CMD BATCH --vanilla --slave";

		// the path to the R file (strip off the filename):
		// remove prefix '/' iff we are on a windows machine
		String os = System.getProperty("os.name").toLowerCase();
		String rScript = Forum.class.getResource("CreateVitaminPlot.R").getFile().substring(0 <= os.indexOf("win") ? 1 : 0);
		String rPath = rScript.substring(0, rScript.length() - "CreateVitaminPlot.R".length());

		script.append("setwd('" + rPath + "')");
		script.append("pngVitaminFile = '" + getModel().vitamine.getRealPath() + "'");
		script.append("pngHealthFile = '" + getModel().vitaminePlus.getRealPath() + "'");
		script.append("age = " + getModel().getAge());
		script.append("sex = '" + getModel().getWebGender() + "'");
		script.append("nutr = NULL");
		for (ConsumedProduct cp : getModel().getConsumedProductList())
		{
			if (!cp.getRemoved())
			{
				script.append("nutr = rbind(nutr, c(pid = " + cp.getProduct_Id() + ", weight = " + cp.getWeight() + "))");
			}
		}

		script.append("source('" + rScript + "')");

		try
		{
			script.execute();
			System.out.println("Errors: " + script.getErrors());
			System.out.println("Output: " + script.getOutput());
			System.out.println("Result: " + script.getResult());
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// so far so good, so figs are not dirty anymore
		getModel().dirtyVitaminPlots = false;

		// also save new figs in db if logged in
		getModel().updateFiguresDB(db);
	}

	private void setFoodFigsDirtyState(Database db) throws DatabaseException
	{
		getModel().vitamine.erase();
		getModel().vitaminePlus.erase();

		if (getModel().anyConsumedProduct())
		{
			getModel().vitamine.showLoading();
			getModel().vitaminePlus.showLoading();

			getModel().dirtyVitaminPlots = true;
		} else
		{
			getModel().vitamine.showDefault();
			getModel().vitaminePlus.showDefault();

			getModel().dirtyVitaminPlots = false;
		}

		// if logged in then also remove Figs from DB
		if (getModel().isLoggedIn())
			getModel().removeFiguresDB(db);
	}

	public void toFoodTabFood(Database db, Tuple request) throws Exception
	{
		// update 'foodTab' to vitamin-tab
		getModel().setScreenFoodTab(getModel().FOOD_VIEW_TAB_FOOD);
		getModel().setScreen(getModel().FOOD_VIEW);// to be sure
	}

	public void toFoodTabExercise(Database db, Tuple request) throws Exception
	{
		// update 'foodTab' to vitamin-tab
		getModel().setScreenFoodTab(getModel().FOOD_VIEW_TAB_EXERCISE);
		getModel().setScreen(getModel().FOOD_VIEW);// to be sure
	}

	public void toFoodTabVitamin(Database db, Tuple request) throws Exception
	{
		// update 'foodTab' to vitamin-tab
		getModel().setScreenFoodTab(getModel().FOOD_VIEW_TAB_VITAMIN);
		getModel().setScreen(getModel().FOOD_VIEW);// to be sure

		if (getModel().dirtyVitaminPlots)
		{
			// if we eat st, then create vitamin image
			if (getModel().anyConsumedProduct())
			{
				this.createVitaminePlot(db);
			}
		}
	}

	public void toFoodTabAdvice(Database db, Tuple request) throws Exception
	{
		// update 'foodTab' to vitamin-tab
		getModel().setScreenFoodTab(getModel().FOOD_VIEW_TAB_ADVICE);
		getModel().setScreen(getModel().FOOD_VIEW);// to be sure
	}
}