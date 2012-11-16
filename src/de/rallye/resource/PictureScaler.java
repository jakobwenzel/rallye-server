package de.rallye.resource;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Felix HŸbner
 * @version 1.0
 * 
 */
public class PictureScaler extends ImageScaler {
	
	
	public static byte[] scaleSameAspect(byte[] pic, int maxWidth, int maxHeight) throws IOException {
		Logger logger =  LogManager.getLogger(PictureScaler.class.getName());
		
		ByteArrayInputStream in = new ByteArrayInputStream(pic);
		
		BufferedImage img = ImageIO.read(in);
		int i_w = img.getWidth(), i_h = img.getHeight();
		
		BufferedImage imageBuff = scaleImage(img, new Dimension(maxWidth,maxHeight));

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		ImageIO.write(imageBuff, "jpg", buffer);

		logger.info("Scale Pic from "+i_w+"x"+i_h+" ("+pic.length+"Byte) to "+imageBuff.getWidth()+"x"+imageBuff.getHeight()+" ("+buffer.size()+" Byte)");
		
		return buffer.toByteArray();
	}

}
