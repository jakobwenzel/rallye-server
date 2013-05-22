package de.rallye.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>
 * A simple 2-level cache for byte[] images consisting of a small and fast
 * in-memory cache (1st level cache) and a slower but bigger disk cache (2nd
 * level cache). For second level caching, the application's cache directory
 * will be used.
 * </p>
 * <p>
 * When pulling from the cache, it will first attempt to load the image from
 * memory. If that fails, it will try to load it from disk. If that succeeds,
 * the image will be put in the 1st level cache and returned. Otherwise it's a
 * cache miss, and the caller is responsible for loading the image from
 * elsewhere (probably the Internet).
 * </p>
 * <p>
 * Pushes to the cache are always write-through (i.e., the image will be stored
 * both on disk and in memory).
 * </p>
 * <p>changed from android to PC, and from bitmap to byte[] by Felix H�bner</p>
 * @author Matthias Kaeppler
 * @author Felix H�bner
 * @version 1.0
 * @source this class copied from
 *         http://www.java2s.com/Open-Source/Android/App/psnfriends
 *         /com/github/droidfu/imageloader/ImageCache.java.htm
 * 
 */
@SuppressWarnings("serial")
public class BlobStore extends LinkedHashMap<String, byte[]> {
	
	private static int firstLevelCacheSize;

	private static String secondLevelCacheDir;

	// set default size to 1 MB
	private static int cacheMaxSize = 1024 * 1024;

	private String compressedImageFormat = "jpg";
	
	private Lock lock = new ReentrantLock();

	private Logger logger = LogManager.getLogger(BlobStore.class);

	/**
	 * 
	 * @param path
	 *            path from the root dir to store the files
	 * @param cacheFolder
	 *            name of the folder to store the files
	 * @param maxFileSize
	 *            in byte
	 * @author Felix H�bner
	 */
	public void initialize(String path, String cacheFolder,int firstLevelSize, int maxFileSize) {

		secondLevelCacheDir = path + "/blobStore/" + cacheFolder;
		new File(secondLevelCacheDir).mkdirs();
		cacheMaxSize = maxFileSize;
		firstLevelCacheSize = firstLevelSize;

		logger.info("Setup Blobstore at: " + secondLevelCacheDir + " - Cache size: " + firstLevelCacheSize + " Elements - Max Blob Size: "
				+ cacheMaxSize + " Byte");
	}

	/**
	 * @param cachedImageQuality
	 *            the quality of images being compressed and written to disk
	 *            (2nd level cache) as a number in [0..100]
	 */
	/*
	 * public static void setCachedImageQuality(int cachedImageQuality) {
	 * ImageCache.cachedImageQuality = cachedImageQuality; }
	 */

	@Override
	public byte[] get(Object key) {
		lock.lock();
		String imageUrl = (String) key;
		byte[] blob = super.get(imageUrl);

		if (blob != null) {
			// 1st level cache hit (memory)
			logger.info("Cache (1st Level) Hit for: " + imageUrl + " FileName: " + getImageFile(imageUrl));
			lock.unlock();
			return blob;
		}

		File imageFile = getImageFile(imageUrl);
		if (imageFile.exists()) {
			// 2nd level cache hit (disk)
			logger.info("Cache (2st Level) Hit for: " + imageUrl + " FileName: " + getImageFile(imageUrl));//TODO: opening files just for debug??
			
			try {
				FileInputStream in = new FileInputStream(imageFile.getAbsolutePath());
				blob = new byte[in.available()];
				logger.trace("estimated length: "+in.available());
				in.read(blob);
				in.close();
			} catch (IOException e) {
				// error while reading -> cache miss
				lock.unlock();
				return null;
			}
			// add to first level cache
			super.put(imageUrl, blob);
			lock.unlock();
			return blob;
		}

		logger.info("Cache Miss for: " + imageUrl + " FileName: " + getImageFile(imageUrl));
		// cache miss
		lock.unlock();
		return null;
	}

	@Override
	public byte[] put(String imageUrl, byte[] image) {
		lock.lock();
		if (image.length <= cacheMaxSize) {
			File imageFile = getImageFile(imageUrl);
			try {
				imageFile.createNewFile();

				FileOutputStream ostream = new FileOutputStream(imageFile);

				ostream.write(image);
				ostream.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			logger.info("Add to Cache: " + imageUrl + " FileName: " + getImageFile(imageUrl));

			lock.unlock();
			return super.put(imageUrl, image);
		} else {
			logger.error("File size to big.");
			lock.unlock();
			throw new IllegalArgumentException("File to big");
		}
	}
	
	@Override
	public byte[] remove(Object key) {
		byte[] value = null;
		lock.lock();
		
		//delete in 1st level cache
		value = super.remove(key);
		
		//delete in 2nd level cache
		String imageUrl = (String) key;
		File f = getImageFile(imageUrl);
		if (f.exists()) {
			try {
				FileInputStream in = new FileInputStream(f.getAbsolutePath());
				value = new byte[in.available()];
				in.read(value);
				in.close();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				logger.catching(e);
			}
			f.delete();
		}
		
		lock.unlock();
		return value;
	}

	private File getImageFile(String imageUrl) {//TODO: why the fuck hexcode? why not just use the damn id
		String fileName = Integer.toHexString(imageUrl.hashCode()) + "." + compressedImageFormat;
		return new File(secondLevelCacheDir + "/" + fileName);
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<String, byte[]> eldest) {
		logger.info("1st LevelCache size: (" + size() + "/" + firstLevelCacheSize + ")");
		return size() > firstLevelCacheSize;
	}
	
	/**
	 * return the status of the Blobstore
	 * @return
	 */
	public String getStatus() {
		return "1st LevelCache size: (" + size() + "/" + firstLevelCacheSize + ")\n2nd LevelCache size: "+(new File(secondLevelCacheDir).list().length);
	}
}
