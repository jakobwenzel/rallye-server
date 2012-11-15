package de.rallye.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Felix HŸbner
 * @version 1.0
 * @source this class copied from
 *         http://www.java2s.com/Open-Source/Android/App/psnfriends
 *         /com/github/droidfu/imageloader/ImageCache.java.htm but for byte[]
 *         arrays
 * 
 */
public class BlobStore extends LinkedHashMap<String, byte[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5632921081886680279L;
	
	static int firstLevelCacheSize = 10;

	private static String secondLevelCacheDir;
	
	// set default size to 1 MB
	private static int cacheMaxSize = 1024*1024;

	private String compressedImageFormat = "jpg";
	
	private Logger logger =  LogManager.getLogger(this.getClass().getName());

	/**
	 * 
	 * @param path path from the root dir to store the files
	 * @param cacheFolder name of the folder to store the files
	 * @param maxFileSize in byte
	 * @author Felix HŸbner
	 */
	public void initialize(String path,String cacheFolder,int maxFileSize) {
		
		secondLevelCacheDir = path + "/blobStore/"+ cacheFolder;
		new File(secondLevelCacheDir).mkdirs();
		cacheMaxSize = maxFileSize;
		
		logger.info("Setup Blobstore at: "+secondLevelCacheDir+" - Cache size: "+firstLevelCacheSize+" Elements - Max Blob Size: "+cacheMaxSize+" Byte");
	}

	/**
	 * @param cachedImageQuality
	 *            the quality of images being compressed and written to disk
	 *            (2nd level cache) as a number in [0..100]
	 */
	/*public static void setCachedImageQuality(int cachedImageQuality) {
		ImageCache.cachedImageQuality = cachedImageQuality;
	}*/

	@Override
	public byte[] get(Object key) {
		String imageUrl = (String) key;
		byte[] blob = super.get(imageUrl);
		

		if (blob != null) {
			// 1st level cache hit (memory)
			logger.info("Cache (1st Level) Hit for: "+imageUrl+" FileName: "+getImageFile(imageUrl));
			return blob;
		}

		File imageFile = getImageFile(imageUrl);
		if (imageFile.exists()) {
			// 2nd level cache hit (disk)
			logger.info("Cache (2st Level) Hit for: "+imageUrl+" FileName: "+getImageFile(imageUrl));
			blob = new byte[cacheMaxSize];
			try {
				FileInputStream in = new FileInputStream(imageFile.getAbsolutePath());
				in.read(blob);
				in.close();
			} catch (IOException e) {
				//error while reading -> cache miss
				return null;
			}
			//add to first level cache
			super.put(imageUrl, blob);
			return blob;
		}
		
		logger.info("Cache Miss for: "+imageUrl+" FileName: "+getImageFile(imageUrl));
		// cache miss
		return null;
	}

	@Override
	public byte[] put(String imageUrl, byte[] image) {
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
		
		logger.info("Add to Cache: "+imageUrl+" FileName: "+getImageFile(imageUrl));

		return super.put(imageUrl, image);
	}

	private File getImageFile(String imageUrl) {
		String fileName = Integer.toHexString(imageUrl.hashCode()) + "." + compressedImageFormat;
		return new File(secondLevelCacheDir + "/" + fileName);
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<String, byte[]> eldest) {
		logger.info("1st LevelCache size: ("+size()+"/"+firstLevelCacheSize+")");
		return size() > firstLevelCacheSize;
	}
}
