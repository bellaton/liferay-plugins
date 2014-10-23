/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.resourcesimporter.util;

import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.Group;
import com.liferay.portal.service.GroupLocalServiceUtil;

import java.net.URL;
import java.net.URLConnection;

import java.util.Set;

import javax.servlet.ServletContext;

/**
 * @author Michael C. Han
 */
public class ImporterFactory {

	public static final String RESOURCES_DIR =
		"/WEB-INF/classes/resources-importer/";

	public static final String TEMPLATES_DIR =
		"/WEB-INF/classes/templates-importer/";

	public static ImporterFactory getInstance() {
		return _instance;
	}

	public Importer createImporter(
			long companyId, ServletContext servletContext, String resourcesDir)
		throws Exception {

		Set<String> resourcePaths = servletContext.getResourcePaths(
			RESOURCES_DIR);
		Set<String> templatePaths = servletContext.getResourcePaths(
			TEMPLATES_DIR);

		URL privateLARURL = null;
		URL publicLARURL = servletContext.getResource(
			RESOURCES_DIR.concat("archive.lar"));

		if (publicLARURL == null) {
			privateLARURL = servletContext.getResource(
				RESOURCES_DIR.concat("private.lar"));

			publicLARURL = servletContext.getResource(
				RESOURCES_DIR.concat("public.lar"));
		}

		Importer importer = null;

		if ((privateLARURL != null) || (publicLARURL != null)) {
			LARImporter larImporter = getLARImporter();

			URLConnection privateLARURLConnection = null;

			if (privateLARURL != null) {
				privateLARURLConnection = privateLARURL.openConnection();

				larImporter.setPrivateLARInputStream(
					privateLARURLConnection.getInputStream());
			}

			URLConnection publicLARURLConnection = null;

			if (publicLARURL != null) {
				publicLARURLConnection = publicLARURL.openConnection();

				larImporter.setPublicLARInputStream(
					publicLARURLConnection.getInputStream());
			}

			importer = larImporter;
		}
		else if ((resourcePaths != null) && !resourcePaths.isEmpty()) {
			importer = getResourceImporter();

			importer.setResourcesDir(RESOURCES_DIR);
		}
		else if ((templatePaths != null) && !templatePaths.isEmpty()) {
			importer = getResourceImporter();

			Group group = GroupLocalServiceUtil.getCompanyGroup(companyId);

			importer.setGroupId(group.getGroupId());
			importer.setResourcesDir(TEMPLATES_DIR);
		}
		else if (Validator.isNotNull(resourcesDir)) {
			importer = getFileSystemImporter();

			importer.setResourcesDir(resourcesDir);
		}

		if (importer == null) {
			throw new ImporterException("No valid importer found");
		}

		return importer;
	}

	protected FileSystemImporter getFileSystemImporter() {
		return new FileSystemImporter();
	}

	protected LARImporter getLARImporter() {
		return new LARImporter();
	}

	protected ResourceImporter getResourceImporter() {
		return new ResourceImporter();
	}

	private static ImporterFactory _instance = new ImporterFactory();

}