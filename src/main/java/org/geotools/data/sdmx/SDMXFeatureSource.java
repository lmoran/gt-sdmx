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
import java.util.HashSet;
import java.util.logging.Level;

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
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.PortableDataSet;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;

/**
 * Source of features for the ArcGIS ReST API
 * 
 * @author lmorandini
 *
 */
public class SDMXFeatureSource extends ContentFeatureSource {

  // FIXME:
  protected CoordinateReferenceSystem crs;

  protected SDMXDataStore dataStore;
  protected DefaultResourceInfo resInfo;
  protected Dataflow dataflow;
  protected DataFlowStructure dataflowStructure;

  public SDMXFeatureSource(ContentEntry entry, Dataflow dataflowIn, Query query)
      throws IOException, FactoryException {

    super(entry, query);
    this.dataStore = (SDMXDataStore) entry.getDataStore();
    this.dataflow = dataflowIn;

    // FIXME: 4326 only for now
    this.crs = CRS.parseWKT(
        "GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4326\"]]");
  }

  @Override
  protected SimpleFeatureType buildFeatureType() throws IOException {

    // Sets the information about the resource
    this.resInfo = new DefaultResourceInfo();
    try {
      this.resInfo
          .setSchema(new URI(this.dataStore.getNamespace().toExternalForm()));
    } catch (URISyntaxException e) {
      // Re-packages the exception to be compatible with method signature
      throw new IOException(e.getMessage(), e.fillInStackTrace());
    }

    this.resInfo.setCRS(this.crs);
    this.resInfo.setKeywords(new HashSet());

    // FIXME: the abstract of the feature type is not set
    this.resInfo.setDescription(this.dataflow.getDescription());

    this.resInfo.setTitle(this.dataflow.getName());
    this.resInfo.setName(this.dataflow.getId());
    this.resInfo.setCRS(this.crs);
    ReferencedEnvelope geoBbox = new ReferencedEnvelope(-180, 180, -90, 90,
        this.resInfo.getCRS());
    this.resInfo.setBounds(geoBbox);

    // Builds the feature type
    SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
    builder.setCRS(this.resInfo.getCRS()); // NOTE: this has ot be done before
                                           // other settings, lest the SRS is
                                           // not set
    builder.setName(this.entry.getName());
    // FIXME: the abstract of the feature type is not set
    builder.setDescription(
        new SimpleInternationalString(this.resInfo.getDescription()));

    this.dataflowStructure = this.dataStore
        .getDataFlowStructure(this.entry.getName().getLocalPart());

    this.dataflowStructure.getDimensions().forEach(dim -> {
      if (SDMXDataStore.MEASURE_KEY.equals(dim.getId().toUpperCase())) {
        dim.getCodeList().getCodes().entrySet().forEach(entry -> {
          builder.add(entry.getKey(), java.lang.Double.class);
        });
      } else {
        builder.add(dim.getId(), java.lang.String.class);
      }
    });

    /*
     * DIMENSION: dimId: LGA_2011 dimName: Local Government Areas - 2011
     * dimCLId: CL_ABS_SEIFA_LGA_LGA_2011 dimCLFullId:
     * ABS/CL_ABS_SEIFA_LGA_LGA_2011 DIMENSION: dimId: INDEX_TYPE dimName: Index
     * Type dimCLId: CL_ABS_SEIFA_LGA_INDEX_TYPE dimCLFullId:
     * ABS/CL_ABS_SEIFA_LGA_INDEX_TYPE DIMENSION: dimId: MEASURE dimName:
     * Measure dimCLId: CL_ABS_SEIFA_LGA_MEASURE dimCLFullId:
     * ABS/CL_ABS_SEIFA_LGA_MEASURE
     */

    this.schema = builder.buildFeatureType();

    // XXX
    this.schema.getAttributeDescriptors().forEach(descr -> {
      System.out
          .println(descr.getLocalName() + " " + descr.getType().toString());
    });
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
     * Count cnt; Map<String, Object> params = new HashMap<String, Object>(
     * SDMXDataStore.DEFAULT_PARAMS); params.put(SDMXDataStore.COUNT_PARAM,
     * true); params.put(SDMXDataStore.GEOMETRY_PARAM,
     * this.composeExtent(this.getBoundsInternal(query)));
     * 
     * try { cnt = (new Gson()).fromJson(
     * SDMXDataStore.InputStreamToString(this.dataStore .retrieveJSON("POST",
     * (new URL(this.composeQueryURL())), params)), Count.class); } catch
     * (HTTPException e) { throw new IOException( "Error " + e.getStatusCode() +
     * " " + e.getMessage()); }
     * 
     * return cnt == null ? -1 : cnt.getCount();
     */
    return 1; // XXX
  }

  @Override
  protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
      Query query) throws IOException {

    return new SDMXFeatureReader(this.dataStore.getSDMXClient(), this.schema,
        this.dataflow, this.dataflowStructure, query,
        this.dataStore.getLogger());
  }

}
