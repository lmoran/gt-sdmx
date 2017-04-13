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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.opengis.feature.type.Name;
import org.geotools.feature.NameImpl;
import org.geotools.util.UnsupportedImplementationException;

import sun.misc.IOUtils;

/**
 * Main class of the data store
 * 
 * @author lmorandini
 *
 */
public class SDMXDataStore extends ContentDataStore {

  // Cache of feature sources
  protected Map<Name, SDMXFeatureSource> featureSources = new HashMap<Name, SDMXFeatureSource>();

  // Default feature type geometry attribute
  public static final String GEOMETRY_ATTR = "geometry";

  protected URL namespace;
  protected URL apiUrl;
  protected String user;
  protected String password;

  public SDMXDataStore(String namespaceIn, String apiEndpoint, String user,
      String password) throws MalformedURLException, IOException {

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

    // TBD
  }

  @Override
  protected List<Name> createTypeNames() {

    if (this.entries.isEmpty() == false) {
      return new ArrayList<Name>(this.entries.keySet());
    }

    /*
    final List<Dataset> datasetList = this.getCatalog().getDataset();
    List<Name> typeNames = new ArrayList<Name>();

    // Builds a list of calls to be made to retrieve FeatureServer web services
    // metadata that support the ReST API (if there are not distribution
    // elements, it
    // is supposed NOT to support it)
    try {
      Collection<WsCall> calls = new ArrayList<WsCall>();
      datasetList.stream().forEach((ds) -> {
        if (ds.getWebService().toString().contains(FEATURESERVER_SERVICE)) {
          calls.add(new WsCall(ds));
        }
      });

      List<Future<WsCallResult>> futures = executor.invokeAll(calls,
          (REQUEST_TIMEOUT * calls.size()) / REQUEST_THREADS, TimeUnit.SECONDS);

      for (Future<WsCallResult> future : futures) {

        WsCallResult result = future.get();

        // Checks whether the lasyer supports query and JSON
        // TODO: I am not quite sure this catches cases in which ESRI JSON is
        // supporte, but NOT GeoJSON
        if (result != null
            && result.webservice.getSupportedQueryFormats().toLowerCase()
                .contains(FORMAT_JSON.toLowerCase())
            && result.webservice.getCapabilities().toLowerCase()
                .contains(CAPABILITIES_QUERY.toLowerCase())) {
          Name dsName = new NameImpl(namespace.toExternalForm(),
              result.webservice.getName());
          ContentEntry entry = new ContentEntry(this, dsName);
          this.datasets.put(dsName, result.dataset);
          this.entries.put(dsName, entry);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Shutdowsn the executor thread pool
    executor.shutdown();
*/
    // Returns the list of datastore entries
    return new ArrayList<Name>(this.entries.keySet());
  }

  @Override
  protected ContentFeatureSource createFeatureSource(ContentEntry entry)
      throws IOException {

    SDMXFeatureSource featureSource = this.featureSources.get(entry.getName());
    if (featureSource == null) {
      featureSource = new SDMXFeatureSource(entry, new Query());
      this.featureSources.put(entry.getName(), featureSource);
    }

    return featureSource;
  }

  public URL getNamespace() {
    return namespace;
  }

  // TODO: ?
  @Override
  public void dispose() {
    super.dispose();
  }

}
