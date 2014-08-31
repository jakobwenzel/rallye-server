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
