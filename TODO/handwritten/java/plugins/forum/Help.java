package plugins.forum;

import java.util.ArrayList;
import java.util.List;

public class Help
{
	public List<Question> questionList = new ArrayList<Question>();
	
	public Help()
	{
		questionList.add(new Question("ProdNA",
				"Ik kan een product niet vinden",
				"Dat kan kloppen, maar het kan ook zijn dat het product waarnaar je zoekt onder een andere naam " +
				"beschikbaar is. Bijvoorbeeld, een <B>boterham</B> kun je vinden onder <B><I>Broodje</I></B>. " +
				"Van <B>thee</B> zijn voorlopig alleen nog <B><I>Kruidenthee, Pepermuntthee en Vruchtenthee</I></B> beschikbaar. " +
				"<P ALIGN=\"justify\">We zijn van plan om de Voedingsdagboek-database op te schonen en zullen het je laten weten als het zover is. " +
				"Maar we richten ons eerst op het uitbreiden van de website.</P>"));
//		questionList.add(new Question("Amount",
//				"Hoe kan ik de hoeveelheid van een consumptie aanpasssen?",
//				"Je kan de hoeveelheid van een consumptie als volgt wijzigen. " +
//				"Selecteer de consumptie en voer de gewenste hoeveelheid in. " +
//				"Hierbij kun je kiezen of je de hoeveelheid aangeeft in porties of in grammen. Als je niets aangeeft dan gaan we uit van 1 portie. " +
//				"Klik nu op toevoegen om de hoeveelheid van de geselecteerde consumptie aan te passen."));
	}
}
