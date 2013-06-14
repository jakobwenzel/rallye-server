package de.rallye.resources;

public enum Size {
	
	Thumbnail, Standard, Original;
	
	private static final String[] abr =  new String[]{ "t", "m", "l" };
	
	public static Size fromString(String s) {
		for(int i=0; i<abr.length; i++) {
			if (abr[i].equals(s))
				return Size.values()[i];
		}
		return null;
	}
	
	public String toCharString() {
		return abr[this.ordinal()];
	}
	
	public static class SizeString {
		
		final public Size size;
		
		public SizeString(String s) {
			Size size;
			
			try {
				size = Size.valueOf(s);
				if (size == null)
					size = Size.fromString(s);
			} catch (Exception e) {
				size = Size.Standard;
			}
			this.size = size;
		}
	}
}
