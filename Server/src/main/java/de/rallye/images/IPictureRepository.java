/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.images;

import de.rallye.exceptions.DataException;
import de.rallye.model.structures.PictureSize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Ramon on 05.10.2014.
 */
public interface IPictureRepository {

	IPicture getImage(String pictureHash);

	IPicture putImage(int userID, String pictureHash, File file) throws DataException;
	IPicture putImagePreview(int userID, String pictureHash, File file) throws DataException;

	void remove(String pictureHash);

	interface IPicture {
			long lastModified();
		boolean isAvailable(PictureSize size);
		File getFile(PictureSize size);
		boolean isCached(PictureSize size);
		byte[] getCached(PictureSize size);
		InputStream getInputStream(PictureSize size) throws FileNotFoundException;

		int getPictureID();
	}
}
