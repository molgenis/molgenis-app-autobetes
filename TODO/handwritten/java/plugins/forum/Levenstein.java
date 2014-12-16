package plugins.forum;

public class Levenstein
{
	public static Integer getLevenshteinPercentage(String s, String t)
	{
		if (s == null || t == null)
		{
			throw new IllegalArgumentException("Strings must not be null");
		}

		int n = s.length();
		int m = t.length();

		if (n == 0)
		{
			return m;
		}

		else if (m == 0)
		{
			return n;
		}

		int p[] = new int[n + 1];
		int d[] = new int[n + 1];
		int _d[];
		int i;
		int j;

		char t_j;

		int cost;

		for (i = 0; i <= n; i++)
		{
			p[i] = i;
		}

		for (j = 1; j <= m; j++)
		{
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++)
			{
				cost = s.charAt(i - 1) == t_j ? 0 : 1;

				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}
			_d = p;
			p = d;
			d = _d;
		}

		// Determine percentage difference
		double levNum = (double) p[n];
		double percent = (levNum / Math.max(s.length(), t.length())) * 100;
		int percentDiff = (int) percent;

		return percentDiff;
	}
}
