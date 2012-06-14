
package biz.vnc.util;


public class SimpleGetopt
{
	private String optstring = null;
	private String[] args = null;
	private int argindex = 0;
	private String optarg = null;

	public SimpleGetopt(String[] args, String optstring)
	{
		this.args = args;
		this.optstring = optstring;
	}

	public int nextopt()
	{
		int argChar = -1;
		for (int counter = this.argindex; counter < this.args.length; ++counter)
		{
			if ((args[counter] != null) && (args[counter].length() > 1)
			        && (args[counter].charAt(0) == '-'))
			{
				int charIndex = 0;

				argChar = args[counter].charAt(1);
				charIndex = this.optstring.indexOf(argChar);
				this.optarg = null;
				if (charIndex != -1)
				{
					this.argindex = counter + 1;

					if ((this.optstring.length() > (charIndex + 1))
					        && (this.optstring.charAt(charIndex + 1) == ':'))
					{
						if (args[counter].length() > 2)
						{
							this.optarg = args[counter].substring(2).trim();
						}
						else if (args.length > (counter + 1))
						{
							this.optarg = args[counter + 1];
							++this.argindex;
						}
					}
				}
				break;
			}
		}
		return argChar;
	}

	public String getOptarg()
	{
		return this.optarg;
	}
}
