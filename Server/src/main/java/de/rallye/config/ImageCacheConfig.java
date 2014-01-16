package de.rallye.config;

/**
 * Created with IntelliJ IDEA.
 * User: Ramon
 * Date: 01.09.13
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class ImageCacheConfig {

	public final int maxThumbEntries;
	public final int maxMiniEntries;

	public ImageCacheConfig(int maxThumbEntries, int maxMiniEntries) {
		this.maxThumbEntries = maxThumbEntries;
		this.maxMiniEntries = maxMiniEntries;
	}

	public ImageCacheConfig() {
		maxMiniEntries = 25;
		maxThumbEntries = 100;
	}

	@Override
	public String toString() {
		return "ImageCacheConfig "+ maxThumbEntries +","+ maxMiniEntries;
	}
}
