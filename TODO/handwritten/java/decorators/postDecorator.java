/* Date:        April 14, 2013
 * Template:	MapperDecoratorGen.java.ftl
 * generator:   org.molgenis.generators.db.MapperDecoratorGen 4.0.0-testing
 *
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package decorators;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;

public class postDecorator<E extends lmd.Post> extends MapperDecorator<E>
{
	// Mapper is the generate thing
	public postDecorator(Mapper generatedMapper)
	{
		super(generatedMapper);
	}

	public static String escape(String s) {
	    StringBuilder builder = new StringBuilder();
	    boolean previousWasASpace = false;
	    for( char c : s.toCharArray() ) {
	        if( c == ' ' ) {
	            if( previousWasASpace ) {
	                builder.append("&nbsp;");
	                previousWasASpace = false;
	                continue;
	            }
	            previousWasASpace = true;
	        } else {
	            previousWasASpace = false;
	        }
	        switch(c) {
	            case '<': builder.append("&lt;"); break;
	            case '>': builder.append("&gt;"); break;
	            case '&': builder.append("&amp;"); break;
	            case '"': builder.append("&quot;"); break;
	            case '\n': builder.append("<br>"); break;
	            // We need Tab support here, because we print StackTraces as HTML
	            case '\t': builder.append("&nbsp; &nbsp; &nbsp;"); break;  
	            default:
	                if( c < 128 ) {
	                    builder.append(c);
	                } else {
	                    builder.append("&#").append((int)c).append(";");
	                }    
	        }
	    }
	    return builder.toString();
	}
	
	public int add(List<E> entities) throws DatabaseException
	{
		// add your pre-processing here, e.g.
		for (lmd.Post p : entities)
		{
//			// replace enters by <BR>, but only if not done yet.. (this is a bit
//			// buggy)
//			if (!p.getMessage().contains("<BR>"))
//			{
//				p.setMessage(p.getMessage().replaceAll("(\r\n|\n)", "<BR>"));
//			}
			
//			// same: surround with <P> </P>
//			if (!p.getMessage().contains("<P>")){
//				p.setMessage("<P>"+p.getMessage()+"</P>");
//			}
			
//			String escapedMessage = escapeHtml(p.getMessage());
//			
//			// replace [citaat=x] with <QUOTE>message x</QUOTE>
//			
//			
//			p.setMessage(escapedMessage);
		}

		// here we call the standard 'add'
		int count = super.add(entities);

		// add your post-processing here
		// if you throw and exception the previous add will be rolled back

		return count;
	}

	@Override
	public int update(List<E> entities) throws DatabaseException
	{

		// add your pre-processing here, e.g.
		// for (pompgemak.Post e : entities)
		// {
		// e.setTriggeredField("Before update called!!!");
		// }

		// here we call the standard 'update'
		int count = super.update(entities);

		// add your post-processing here
		// if you throw and exception the previous add will be rolled back

		return count;
	}

	@Override
	public int remove(List<E> entities) throws DatabaseException
	{
		// add your pre-processing here

		// here we call the standard 'remove'
		int count = super.remove(entities);

		// add your post-processing here, e.g.
		// if(true) throw new
		// SQLException("Because of a post trigger the remove is cancelled.");

		return count;
	}
}
