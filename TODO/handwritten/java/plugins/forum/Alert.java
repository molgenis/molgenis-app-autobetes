package plugins.forum;

public class Alert
{
	ForumModel model;
	public final String ERROR = "ERROR";
	public final String SUCCESS = "SUCCESS";
	public final String WARNING = "WARNING";
	public final String LOGIN_FAILED = "LOGIN_FAILED";
	public final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
	public final String ACCOUNT_NOT_ACTIVATED = "ACCOUNT_NOT_ACTIVATED";
	public final String ACCOUNT_ALREADY_ACTIVATED = "ACCOUNT_ALREADY_ACTIVATED";
	public final String NEW_TOPIC_SUCCESS = "NEW_TOPIC_SUCCESS";
	public final String NEW_TOPIC_DUPLICATE = "NEW_TOPIC_DUPLICATE";
	public final String REGISTRATION_SUCCESS = "REGISTRATION_SUCCESS";
	public final String UPDATE_ACCOUNT_SUCCESS = "UPDATE_ACCOUNT_SUCCESS";
	public final String EMAIL_FAILED = "EMAIL_FAILED";
	public final String EMAIL_EXISTS = "EMAIL_EXISTS";
	public final String RESEND_PASSWORD_SUCCESS = "RESEND_PASSWORD_SUCCESS";
	public final String EMAIL_RESEND_FAILURE = "EMAIL_RESEND_FAILURE";
	public final String FEEDBACK_SUCCESS = "FEEDBACK_SUCCESS";
	public final String FEEDBACK_FAILED = "FEEDBACK_FAILED";
	public final String PRODUCT_UNKNOWN = "PRODUCT_UNKNOWN";
	public final String ERROR_MULTIPLE_PRODUCTS_SAME_NAME = "ERROR_MULTIPLE_PRODUCTS_SAME_NAME";
	public final String CONSUMPTION_ADDED = "PRODUCT_ADDED";
	public final String CONSUMPTIONS_REMOVED = "CONSUMPTIONS_REMOVED";
	public final String AMOUNT_CHANGED = "AMOUNT_CHANGED";
	public final String AMOUNT_CHANGED_ERROR_SELECTION_TOO_BIG = "AMOUNT_CHANGED_ERROR_SELECTION_TOO_BIG";
	public final String CONSUMPTION_UPDATED_NO_AMOUNT_ERROR = "CONSUMPTION_UPDATED_NO_AMOUNT_ERROR";
	public final String CONSUMPTION_NOT_UPDATED_EQUAL_AMOUNT_WARNING = "CONSUMPTION_NOT_UPDATED_EQUAL_AMOUNT_WARNING";
	public final String DISH_REMOVED = "DISH_REMOVED";
	public final String INGREDIENT_REMOVED = "INGREDIENT_REMOVED";
	public final String INGREDIENT_ADDED = "INGREDIENT_ADDED";
	public final String REMOVE_WEIGHTED_FOOD_FAILED = "REMOVE_WEIGHTED_FOOD_FAILED";
	public final String WEIGHTED_FOOD_AMOUNT_UPDATED = "WEIGHTED_FOOD_AMOUNT_UPDATE";
	public final String WEIGHTED_FOOD_AMOUNT_UPDATE_FAILED = "WEIGHTED_FOOD_AMOUNT_UPDATE_FAILED";

	private boolean show = false;
	private int showCount = 0; // Unfortunately, Molgenis loads screen twice,
								// after which we remove the message
	private String type = ERROR;
	private String title = "Onbekende fout";
	private String body = "Neem aub contact met ons op als deze fout zich blijft voordoen.";

	public Alert(ForumModel forumModel)
	{
		model = forumModel;
	}

	public void setMessageType(String type)
	{
		this.showCount = 0;

		if (WEIGHTED_FOOD_AMOUNT_UPDATE_FAILED.equals(type))
		{
			this.type = ERROR;
			this.title = "Aangepassen hoeveelheid mislukt!";
			this.body = "";
		} else if (WEIGHTED_FOOD_AMOUNT_UPDATED.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Je hebt de hoeveelheid aangepast!";
			this.body = "";
		} else if (REMOVE_WEIGHTED_FOOD_FAILED.equals(type))
		{
			this.type = ERROR;
			this.title = "Verwijderen mislukt!";
			this.body = "";
		} else if (INGREDIENT_ADDED.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Je hebt een ingredi&euml;nt toegevoegd!";
			this.body = "";
		} else if (INGREDIENT_REMOVED.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Je hebt een ingredi&euml;nt verwijderd!";
			this.body = "";
		} else if (DISH_REMOVED.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Je hebt een gerecht verwijderd!";
			this.body = "";
		} else if (CONSUMPTION_NOT_UPDATED_EQUAL_AMOUNT_WARNING.equals(type))
		{
			this.type = WARNING;
			this.title = "Niets veranderd!";
			this.body = "Je wilde de hoeveelheid aanpassen, maar hebt dat niet gedaan. Probeer het nog eens :-)";
		} else if (CONSUMPTION_UPDATED_NO_AMOUNT_ERROR.equals(type))
		{
			this.type = ERROR;
			this.title = "Aanpassen hoeveelheid mislukt!";
			this.body = "Je hebt geen (juiste) hoeveelheid ingevuld bij Porties of Gewicht. Gebruik getallen zoals '1.2' of '1,2' of '100'.";
		} else if (AMOUNT_CHANGED.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Je hebt de hoeveelheid van je consumptie aangepast!";
			this.body = "";
		} else if (AMOUNT_CHANGED_ERROR_SELECTION_TOO_BIG.equals(type))
		{
			this.type = ERROR;
			this.title = "Aanpassen hoeveelheid mislukt!";
			this.body = "Je had meerdere consumpties tegelijk geselecteerd. Selecteer 1 consumptie en pas de hoeveelheid aan.";
		} else if (CONSUMPTIONS_REMOVED.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Consumptie verwijderd!";
			this.body = "";
		} else if (CONSUMPTION_ADDED.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Voeding toevoegen gelukt!";
			this.body = "";
		} else if (ERROR_MULTIPLE_PRODUCTS_SAME_NAME.equals(type))
		{
			this.type = ERROR;
			this.title = "Voedingsaanduiding dubbelzinnig";
			this.body = "Er zijn meerdere producten/gerechten bekend met dezelfde naam. Wij weten dus niet welke u bedoelt. Geef de producten/gerechten verschillende namen.";
		} else if (PRODUCT_UNKNOWN.equals(type))
		{
			this.type = ERROR;
			this.title = "Voeding onbekend!";
			this.body = "De voeding die u probeert op te slaan is onbekend. Probeer het nog eens.";
		} else if (FEEDBACK_SUCCESS.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Feedback verstuurd!";
			this.body = "";
		} else if (FEEDBACK_FAILED.equals(type))
		{
			this.type = ERROR;
			this.title = "Feedback versturen mislukt!";
			this.body = "Om onbekende oorzaak is het niet gelukt om je feedback te versturen.";
		} else if (UPDATE_ACCOUNT_SUCCESS.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Je hebt je gegevens bijgewerkt!";
			this.body = "";
		} else if (ACCOUNT_ALREADY_ACTIVATED.equals(type))
		{
			this.type = ERROR;
			this.title = "Je account is al geactiveerd!";
			this.body = "Log in met je e-mailadres en wachtwoord. Ben je je wachtwoord vergeten? Klik dan op 'toesturen', in de tekst onder de 'Log-in'-knop.";
		} else if (ACCOUNT_NOT_ACTIVATED.equals(type))
		{
			this.type = ERROR;
			this.title = "Activeer eerst je account!";
			this.body = "Inloggen is mislukt omdat je je account nog niet geactiveerd hebt.<BR><BR>Nadat je aanmelding hebben we je een email met een activatielink gestuurd. Klik eerst op die link!";
		} else if (REGISTRATION_SUCCESS.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Je bent aangemeld!";
			this.body = "We hebben je een email gestuurd met een activatielink.<BR><BR><I><B>Pas op!</B> Het kan zijn dat deze e-mail bij je 'ongewenste berichten' of 'spam' terecht gekomen is.</I>";
		} else if (EMAIL_RESEND_FAILURE.equals(type))
		{
			this.type = ERROR;
			this.title = "Mislukt!";
			this.body = "Het lukte ons niet om je een email met je gegevens te sturen. Heb je het juiste e-mailadres ingevoerd?<BR><BR> Als deze problemen blijven aanhouden, vertel ons dan dat wij iets fout doen via 'Feedback' svp. Hartelijk dank!";
		} else if (RESEND_PASSWORD_SUCCESS.equals(type))
		{
			this.type = SUCCESS;
			this.title = "E-mail versturen gelukt!";
			this.body = "We hebben je account uitgezet en je een email met een nieuwe activatielink gestuurd. Hiermee kun je inloggen. Daarna kun je je wachtwoord wijzigen via 'Mijn gegevens'.";
		} else if (EMAIL_EXISTS.equals(type))
		{
			this.type = ERROR;
			this.title = "Dit e-mailadres betaat al!";
			this.body = "We hebben je account uitgezet en je een email met een nieuwe activatielink gestuurd. Hiermee kun je inloggen. Daarna kun je je wachtwoord wijzigen via 'Mijn gegevens'.";
		} else if (EMAIL_FAILED.equals(type))
		{
			this.type = ERROR;
			this.title = "Email sturen mislukt!";
			this.body = "Gebruik een bestaand e-mailadres. Deed je dat al? Vertel ons dan dat wij iets fout doen via 'Feedback' svp. Hartelijk dank!";
		} else if (LOGIN_FAILED.equals(type))
		{
			this.type = ERROR;
			this.title = "Inloggen mislukt!";
			this.body = "Controleer je e-mailadres en wachtwoord. Ben je je wachtwoord vergeten? Klik dan op 'toesturen', in de tekst onder de 'Log-in'-knop.";
		} else if (LOGIN_SUCCESS.equals(type))
		{
			this.type = SUCCESS;
			this.title = "Je bent ingelogd!";
			this.body = "";
		} else if (NEW_TOPIC_SUCCESS.equals(type))
		{
			this.type = SUCCESS;
			this.title = "U hebt een nieuw onderwerp geplaatst.";
			this.body = "Het staat bovenaan de lijst";
		} else if (NEW_TOPIC_DUPLICATE.equals(type))
		{
			this.type = ERROR;
			this.title = "Deze titel bestaat al!";
			this.body = "Gaat het hier om een nieuw onderwerp? Pas dan je titel aan.<BR><BR> Gaat het hier niet om een nieuw onderwerp? Ga dan 'terug' en plaats je bericht bij de desbetreffende titel.";
		} else
		{
			this.title = "Onbekende fout";
			this.body = "Neem aub contact met ons op als deze fout zich blijft voordoen.";
		}

		this.show = true;
	}

	public boolean show()
	{
		return this.show;
	}

	public boolean isError()
	{
		return ERROR.equals(this.type);
	}

	public boolean isSuccess()
	{
		return SUCCESS.equals(this.type);
	}

	public String getTitle()
	{
		return this.title;
	}

	/**
	 * return body of alert AND set show to false
	 * 
	 * @return
	 */
	public String getBody()
	{
		this.showCount++;
		if (2 == this.showCount)
			this.clear();

		return this.body;
	}

	public void clear()
	{
		this.showCount = 0;
		this.show = false;
	}
}
