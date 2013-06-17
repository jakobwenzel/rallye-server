package de.rallye.resources;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.exceptions.DataException;

public class ImageRepository {
	
	private static final Logger logger = LogManager.getLogger(ImageRepository.class);

	private String repository;
	private ImageCache cache;
	
	private Lock lock = new ReentrantLock();
	
	
	private static class ImageCache extends LinkedHashMap<Integer, BufferedImage> {
		
		private static final long serialVersionUID = 1L;
		
		private final int maxCacheEntries;
		
		public ImageCache(int maxCacheEntries) {
			super(maxCacheEntries, 1);
			this.maxCacheEntries = maxCacheEntries;
		}
		
		@Override
		protected boolean removeEldestEntry(Map.Entry<Integer, BufferedImage> eldest) {
			return size() > maxCacheEntries;
		}

	}
	
	private File getFile(int pictureID, Size size) {
		return new File(repository, pictureID +"_"+ size.toCharString());
	}

	
	public ImageRepository(String repository, int maxCacheEntries) {
		this.repository = repository;
		this.cache = new ImageCache(maxCacheEntries);
		
		new File(this.repository).mkdirs();
	}
	
	public BufferedImage get(int pictureID, Size size) {
		logger.info("Requested "+ pictureID +" in "+ size);
		lock.lock();
		
		BufferedImage img = cache.get(pictureID);
		
		if (img == null) {
			logger.info("Loading "+ pictureID +" from File");
			
			File f = getFile(pictureID, size);
			
			try {
				img = ImageIO.read(f);
				
				cache.put(pictureID, img);
			} catch (IOException e) {
				logger.error("Picture does not exist", e);
			}
		}
		
		lock.unlock();
		return img;
	}
	
	public BufferedImage put(int pictureID, BufferedImage value) {
		
	}
	
	public BufferedImage remove(int pictureID) {
		cache.remove(pictureID);
		
		String name = key.toString();
		
		logger.info("Removing "+ name);
		File f = new File(repository, name);
		f.delete();
		
		return null;
	}	
	
}
