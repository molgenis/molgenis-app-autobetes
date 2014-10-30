package plugins.forum.ExternalData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lmd.Category;
import lmd.Food;
import lmd.Nutrients;
import lmd.Product;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.util.CsvFileReader;
import org.molgenis.util.CsvReader;
import org.molgenis.util.Tuple;

public class ImportData
{
	private static Nutrients getNutrients(Nutrients nut, Product p)
	{
		for (String field : nut.getFields(true))
		{
			try
			{
				nut.set(field, (Double) p.get(field));
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return nut;
	}
	
	public static void main(final Database db) throws DatabaseException, FileNotFoundException, IOException
	{
		System.out.println("ImportData.main (in package login!) (15jun11)");

		// retrieve the path to the external data csv files
		// the name of this file (after compilation!!):
		String theResource = "ImportData.class";
		// the url to this file
		URL url = ImportData.class.getResource(theResource);

		// the path to this file (strip off the prefix / and the filename:
		// remove prefix '/' if we are on a windows machine
		String os = System.getProperty("os.name").toLowerCase();
		String filePath = url.getFile().substring(0 <= os.indexOf("win") ? 1 : 0, url.getFile().length() - theResource.length());

		// compose the path to the data
		File directory = new File(filePath + "data");

		// db.setLogin(new SimpleLogin());

		try
		{
			db.beginTx();

			// to prevent parsing the .svn file
			/*
			 * FilenameFilter filter = new FilenameFilter() { public boolean
			 * accept(File dir, String name) { return !name.startsWith("."); }
			 * };
			 */

			// for (String f : directory.list(filter)) {
			final List<Category> catLst = new ArrayList<Category>();
			final List<Tuple> tupleLst = new ArrayList<Tuple>();

			String nevoFile = "Nevo2006_MD_formatted_incl_units_June09_incl_English_Okt2011_Marijn2.csv";// "Nevo2006_MD_formatted_incl_units_June09.csv";//"Nevo2006_MD_formatted_incl_units_June09_incl_English_Okt2011_Marijn2.csv"
			System.out.println("importing " + nevoFile);
			CsvReader reader = new CsvFileReader(new File(directory + File.separator + nevoFile));
			for (Tuple tuple : reader)
			{
				// add this tuple to the tuple-list
				tupleLst.add(tuple);

				// only add Category if not added yet
				Category cat = new Category();
				cat.setCategoryName(tuple.getString("Category"));
				if (!catLst.contains(cat))
					catLst.add(cat);
			}

			// add the category list (with unique category names) to db
			db.add(catLst);
			System.out.println(">> Categorys added");

			// retrieve the ID's of the Category's such that it is easy to
			// assign
			// a Category to each of the products (via its ID)

			// retrieve Category's:
			Query q = db.query(Category.class);
			List<Category> lc = q.find();

			// fill a hashmap with key/value-pairs = categoryNaam/ID-pairs
			Map mapCat = new HashMap();
			for (Category c : lc)
			{
				mapCat.put(c.getCategoryName(), c.getId());
			}

			// use to easily find products that are in different categories
			Map<String, Product> prodMap = new HashMap<String, Product>();

			final List<Product> prodLst = new ArrayList<Product>();
			for (Tuple t : tupleLst)
			{
				Product p = new Product();
				t.set("Category", (Integer) mapCat.get(t.getString("Category")));
				p.set(t, false);
				p.set__Type("Product");

				if (p.getEenheidGewicht() == null)
					p.setEenheidGewicht(100.0);

				// the naam is OVERWRITTEN in the decorator!
				// p.setNaam(p.getProductNaam() + "[" + p.getProductNaamEngels()
				// + "] (" + p.getEenheid() + " = " +
				// p.getEenheidGewicht().toString() +
				// " gram)");
				p.setName(p.getProductNaam());
				p.setNaam(p.getProductNaam() + " (" + p.getEenheid() + " = " + p.getEenheidGewicht().toString() + " gram)");

				// p.set(2);

				// put any nulls to zero (0)
				for (String field : p.getFields(true))
				{
					// field != category_id && field != created
					if (!(Product.CATEGORY.toLowerCase() + "_id").equals(field) && !Product.CREATED.toLowerCase().equals(field) && p.get(field) == null)
					{
						p.set(field, new Double(0));
					}
				}

				if (prodMap.containsKey(p.getNaam()))
				{
					Product prevProduct = prodMap.get(p.getNaam());
					prevProduct.getCategory_Id().addAll(p.getCategory_Id());
				} else
				{
					prodMap.put(p.getNaam(), p);
					prodLst.add(p);
				}
			}

			// add the product list to db
			int halfsize = prodLst.size() / 2;
			db.add(prodLst.subList(0, halfsize));
			db.add(prodLst.subList(halfsize, prodLst.size()));
			
			// also add the products as 'Food':
			List<Food> foodLst = new ArrayList<Food>();
			List<Nutrients> nutrList = new ArrayList<Nutrients>();
			Iterator<Product> it = prodLst.iterator();
			while (it.hasNext())
			{
				Product p = it.next();
				Food f = new Food();
				
				Nutrients n = getNutrients(new Nutrients(), p);
				nutrList.add(n);
				
				f.setName(p.getProductNaam());
				f.setNameDescriptive(p.getNaam());
				f.setCategory(p.getCategory());
				f.setDishCategory("Product");
				f.setUnit(p.getEenheid());
				f.setUnitWeight(p.getEenheidGewicht());
				f.setNutrients100grams(n);
				
				foodLst.add(f);
			}
			
			db.add(nutrList);
			db.add(foodLst);
			
			/*
			 * original code (until 21dec10): reader.parse(new
			 * CsvReaderListener() { public void handleLine(int line_number,
			 * Tuple tuple) throws Exception { Product prod = new Product();
			 * prod.set__Type("Product"); prod.set(tuple); //
			 * Logger.debug(">>>>>>>>>>" + prod.getvit) if
			 * (prod.getEenheidGewicht() == null) prod.setEenheidGewicht(100.0);
			 * prod.setNaam(prod.getProductNaam() + " (" + prod.getEenheid() +
			 * " = " + prod.getEenheidGewicht().toString() + " gram)");
			 * prod.setMolgenisUser(1); db.add(prod);
			 * 
			 * // only add Category if not added yet if
			 * (!catLst.contains(tuple.getString("Category")))
			 * catLst.add(tuple.getString("Category")); } });
			 */
			// CsvImportByName.importAll(dir);

			/***
			 * ACTIVITIES: f = "Activity.csv"; System.out.println("importing " +
			 * f); reader = new CsvFileReader(new File(directory +
			 * File.separator + f)); reader.parse(new CsvReaderListener() {
			 * public void handleLine(int line_number, Tuple tuple) throws
			 * Exception { ActiviteitType act = new ActiviteitType();
			 * act.set(tuple); act.setMolgenisUser(1); db.add(act);
			 * 
			 * } });
			 ***/

			/***
			 * Create new users (old way) // add user group 'Patient'
			 * MolgenisUserGroup gr = new MolgenisUserGroup(); List<Integer> il
			 * = new ArrayList<Integer>(); il.addAll(Arrays.asList(new Integer[]
			 * { 6, 7, 8, 9, 10, 11, 12, 13, 14, 15})); gr.setAllowedToView(il);
			 * gr.setAllowedToEdit(il); gr.setName("Patient"); db.add(gr);
			 * 
			 * // add user group 'Arts' gr.setAllowedToView(il);
			 * gr.setAllowedToEdit(Arrays.asList(new Integer[] {}));
			 * gr.setName("Arts"); db.add(gr);
			 * 
			 * System.out.println(">> Start adding arts 'k'"); // add first arts
			 * Arts arts = new Arts(); arts.setName("k"); arts.setPassword("k");
			 * arts.setSuperuser(false); arts.setZiekenhuis("Martini");
			 * db.add(arts);
			 * 
			 * // add first gebruiker Gebruiker g = new Gebruiker();
			 * g.setName("a"); g.setPassword("a"); g.setSuperuser(false);
			 * g.setGewicht(72.5); db.add(g);
			 * 
			 * // Gewicht gg = new Gewicht(); // gg.setGebruiker(g.getId()); //
			 * // lg.setLengte(g.getLengte()); // gg.setGewicht(g.getGewicht());
			 * // gg.setMoment(g.getStartdatum()); //
			 * System.out.println("ImportData.java is trying to add: " + gg); //
			 * db.add(gg);
			 * 
			 * // MolgenisUser mu = new MolgenisUser(); // mu.setName("md"); //
			 * mu.setPassword("md"); // mu.setSuperuser(true); // db.add(mu);
			 ***/
			db.commitTx();
		} catch (Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
			db.rollbackTx();
		}

		System.out.println("done with uploading data");
	}
}
