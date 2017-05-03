/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2016, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */

package org.geotools.data.sdmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.client.SDMXClientFactory;
import it.bancaditalia.oss.sdmx.api.GenericSDMXClient;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;

import org.geotools.feature.NameImpl;

/**
 * Main class of the data store
 * 
 * @author lmorandini
 *
 */
public class SDMXDataStore extends ContentDataStore {

  // SDMX query constants
  public static String MEASURE_KEY = "MEASURE";
  public static String REGION_KEY = "REGION";
  public static String ALLCODES_EXP = "*";
  public static String OR_EXP = "+";
  public static String SEPARATOR_EXP = ".";

  // SDMX error codes
  public static int ERROR_NORESULTS= 100;
  
  // Cache of feature sources
  protected Map<Name, SDMXFeatureSource> featureSources = new HashMap<Name, SDMXFeatureSource>();

  protected URL namespace;
  protected URL apiUrl;
  protected String user;
  protected String password;
  protected GenericSDMXClient sdmxClient;
  protected Map<String, Dataflow> dataflows = new HashMap<String, Dataflow>();
  protected Map<String, DataFlowStructure> dataflowStructures = new HashMap<String, DataFlowStructure>();

  public SDMXDataStore(String name, String namespaceIn, String apiEndpoint,
      String user, String password)
      throws MalformedURLException, IOException, SdmxException {

    super();

    try {
      this.namespace = new URL(namespaceIn);
    } catch (MalformedURLException e) {
      LOGGER.log(Level.SEVERE,
          "Namespace \"" + namespaceIn + "\" is not properly formatted", e);
      throw (e);
    }
    try {
      this.apiUrl = new URL(apiEndpoint);
    } catch (MalformedURLException e) {
      LOGGER.log(Level.SEVERE,
          "URL \"" + apiEndpoint + "\" is not properly formatted", e);
      throw (e);
    }
    this.user = user;
    this.password = password;

    // SDMXClientFactory.addProvider(namespaceIn, this.apiUrl, false, false,
    // false, "ABS",
    // false); // TODO
    try {
      this.sdmxClient = SDMXClientFactory.createClient(name);
    } catch (SdmxException e) {
      LOGGER.log(Level.SEVERE, "Cannot create client", e);
      throw (e);
    }

    // TODO: add credentials support
    // this.sdmxClient = new RestSdmxClient(name, new URL(apiEndpoint), false,
    // false, false);
  }

  @Override
  protected List<Name> createTypeNames() {

    // This is to avoid pining the SDMX server every time type names habe to be
    // created
    if (this.entries.isEmpty() == false) {
      return new ArrayList<Name>(this.entries.keySet());
    }

    try {
      dataflows = this.sdmxClient.getDataflows();
    } catch (SdmxException e) {
      e.printStackTrace(); // TODO
      return new ArrayList<Name>();
    }

    this.dataflowStructures.clear();
    dataflows.forEach((s, d) -> {
      try {
        this.dataflowStructures.put(s,
            this.sdmxClient.getDataFlowStructure(d.getDsdIdentifier(), true));
      } catch (SdmxException e) {
        LOGGER.log(Level.SEVERE, "Error getting SDMX DSD", e);
      }
      Name name = new NameImpl(namespace.toExternalForm(), s);
      ContentEntry entry = new ContentEntry(this, name);
      this.entries.put(name, entry);
    });

    return new ArrayList<Name>(this.entries.keySet());
  }

  @Override
  protected ContentFeatureSource createFeatureSource(ContentEntry entry)
      throws IOException {

    SDMXFeatureSource featureSource = this.featureSources.get(entry.getName());
    if (featureSource == null) {
      try {
        featureSource = new SDMXFeatureSource(entry,
            this.dataflows.get(entry.getName().getLocalPart()), new Query());
      } catch (FactoryException e) {
        LOGGER.log(Level.SEVERE, "Cannot create CRS", e);
        throw (new IOException(e));
      }
      this.featureSources.put(entry.getName(), featureSource);
    }

    return featureSource;
  }

  public URL getNamespace() {
    return namespace;
  }

  public GenericSDMXClient getSDMXClient() {
    return this.sdmxClient;
  }

  public DataFlowStructure getDataFlowStructure(String name) {
    return this.dataflowStructures.get(name);
  }

  // TODO: ?
  @Override
  public void dispose() {
    super.dispose();
  }

}
