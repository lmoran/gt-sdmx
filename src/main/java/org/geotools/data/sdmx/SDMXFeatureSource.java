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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;

import javax.xml.ws.http.HTTPException;

import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ResourceInfo;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Source of features for the ArcGIS ReST API
 * 
 * @author lmorandini
 *
 */
public class SDMXFeatureSource extends ContentFeatureSource {

  // FIXME: Are we user ArcGIS ReST API always uses this for the "spatial"
  // property?
  protected static CoordinateReferenceSystem SPATIALCRS;

  protected SDMXDataStore dataStore;
  protected DefaultResourceInfo resInfo;
  protected String objectIdField;

  public SDMXFeatureSource(ContentEntry entry, Query query)
      throws IOException {

    super(entry, query);
    this.dataStore = (SDMXDataStore) entry.getDataStore();
  }

  @Override
  protected SimpleFeatureType buildFeatureType() throws IOException {

    // Extracts informaton about the type name (as per this.entry) from the API
    /*
    Dataset ds = this.dataStore.getDataset(this.entry.getName());
    Webservice ws = (new Gson())
        .fromJson(
            SDMXDataStore.InputStreamToString(this.dataStore.retrieveJSON(
                "GET", new URL(ds.getWebService().toString()),
                SDMXDataStore.DEFAULT_PARAMS)),
            Webservice.class);

    if (ws == null) {
      throw new IOException("Type name " + entry.getName() + " not found");
    }

    // Sets the information about the resource
    this.resInfo = new DefaultResourceInfo();
    try {
      this.resInfo
          .setSchema(new URI(this.dataStore.getNamespace().toExternalForm()));
    } catch (URISyntaxException e) {
      // Re-packages the exception to be compatible with method signature
      throw new IOException(e.getMessage(), e.fillInStackTrace());
    }
    try {
      this.resInfo.setCRS(CRS.decode(
          "EPSG:" + ws.getExtent().getSpatialReference().getLatestWkid()));
    } catch (FactoryException e) {
      // FIXME: this is not nice: exceptions should not be re-packaged
      throw new IOException(e.getMessage());
    }

    this.resInfo.setKeywords(new HashSet(ds.getKeyword()));

    // FIXME: the abstract of the feature type is not set
    this.resInfo.setDescription(ds.getDescription());

    this.resInfo.setTitle(ds.getTitle() != null ? ds.getTitle() : ws.getName());
    this.resInfo.setName(ws.getName());
    ReferencedEnvelope geoBbox = new ReferencedEnvelope(
        ws.getExtent().getXmin(), ws.getExtent().getXmax(),
        ws.getExtent().getYmin(), ws.getExtent().getYmax(),
        this.resInfo.getCRS());
    this.resInfo.setBounds(geoBbox);
    this.objectIdField = (ws.getObjectIdField() != null) ? ws.getObjectIdField()
        : ws.getGlobalIdField();

    // Builds the feature type
    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setCRS(this.resInfo.getCRS()); // NOTE: this has ot be done before
                                           // other settings, lest the SRS is
                                           // not set
    builder.setName(this.entry.getName());
    // FIXME: the abstract of the feature type is not set
    builder.setDescription(ds.getDescription() != null
        ? new SimpleInternationalString(ds.getDescription()) : null);

    // Adds non-geometry field descriptions
    ws.getFields().forEach((fld) -> {
      Class clazz = EsriJavaMapping.get(fld.getType());
      if (clazz == null) {
        this.getDataStore().getLogger()
            .severe("Type " + fld.getType() + " not found");
      }
      builder.add(fld.getName(), clazz);
    });

    // Adds the geometry field
    Class clazz = EsriJTSMapping.get(ws.getGeometryType());
    if (clazz == null) {
      this.getDataStore().getLogger()
          .severe("Geometry type " + ws.getGeometryType() + " not found");
    }

    builder.add(SDMXDataStore.GEOMETRY_ATTR, clazz);

    this.schema = builder.buildFeatureType();
    this.schema.getUserData().put("serviceUrl", ds.getWebService());
*/
    return this.schema;
  }

  @Override
  public ResourceInfo getInfo() {
    if (this.resInfo == null) {
      try {
        this.buildFeatureType();
      } catch (IOException e) {
        this.getDataStore().getLogger().log(Level.SEVERE, e.getMessage(), e);
        return null;
      }
    }
    return this.resInfo;
  }

  @Override
  public ContentDataStore getDataStore() {
    return this.dataStore;
  }

  @Override
  public Name getName() {
    return this.entry.getName();
  }

  // TODO: it shuold return the bounds of the query, if not null
  @Override
  protected ReferencedEnvelope getBoundsInternal(Query arg0)
      throws IOException {
    return this.getInfo().getBounds();
  }

  @Override
  protected int getCountInternal(Query query) throws IOException {
/*
    Count cnt;
    Map<String, Object> params = new HashMap<String, Object>(
        SDMXDataStore.DEFAULT_PARAMS);
    params.put(SDMXDataStore.COUNT_PARAM, true);
    params.put(SDMXDataStore.GEOMETRY_PARAM,
        this.composeExtent(this.getBoundsInternal(query)));

    try {
      cnt = (new Gson()).fromJson(
          SDMXDataStore.InputStreamToString(this.dataStore
              .retrieveJSON("POST", (new URL(this.composeQueryURL())), params)),
          Count.class);
    } catch (HTTPException e) {
      throw new IOException(
          "Error " + e.getStatusCode() + " " + e.getMessage());
    }

    return cnt == null ? -1 : cnt.getCount();
*/
    return 1; // XXX
  }

  @Override
  protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
      Query query) throws IOException {

    InputStream result;

    // TBD
    
    // Returns a reader for the result
//    return new SDMXFeatureReader(this.schema, result,
//        this.dataStore.getLogger());
    return null;
  }

  /**
   * Helper method to return an attribute list as the API expects it
   * 
   * @param query
   *          Query to build the attributes for
   */
  protected String composeAttributes(Query query) {

    StringJoiner joiner = new StringJoiner(",");

    // The Object ID is always in to ensure the GeoJSON is correctly processed
    // by the parser,
    // For instance, when the GeoJSON properties is null (i.e., only the
    // geometry is
    // returned), WMS GetMap requests return an empty image
    joiner.add(this.objectIdField);

    if (query.retrieveAllProperties()) {
      Iterator<AttributeDescriptor> iter = this.schema.getAttributeDescriptors()
          .iterator();
      while (iter.hasNext()) {
        AttributeDescriptor attr = iter.next();
        // Skips ID and geometry field
        if (!attr.getLocalName().equalsIgnoreCase(this.objectIdField)
            && !attr.getLocalName().equalsIgnoreCase(
                this.schema.getGeometryDescriptor().getLocalName())) {
          joiner.add(iter.next().getLocalName());
        }
      }
    } else {
      for (String attr : query.getPropertyNames()) {
        // Skips ID and geometry field
        if (!attr.equalsIgnoreCase(this.objectIdField)
            && !attr.equalsIgnoreCase(
                this.schema.getGeometryDescriptor().getLocalName())) {
          joiner.add(attr);
        }
      }
    }

    return joiner.toString();
  }

  /**
   * Compose the query URL of the instance's dataset
   * 
   * @return query URL
   */
  protected String composeQueryURL() {
    return this.schema.getUserData().get("serviceUrl") + "/query";
  }

}
