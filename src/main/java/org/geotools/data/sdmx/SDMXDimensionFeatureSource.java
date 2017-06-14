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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.ResourceInfo;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.IsEqualsToImpl;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.util.SimpleInternationalString;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.And;
import org.opengis.filter.Id;
import org.opengis.filter.Or;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Point;

import it.bancaditalia.oss.sdmx.api.DataFlowStructure;
import it.bancaditalia.oss.sdmx.api.Dataflow;
import it.bancaditalia.oss.sdmx.api.PortableDataSet;
import it.bancaditalia.oss.sdmx.exceptions.SdmxException;
import it.bancaditalia.oss.sdmx.exceptions.SdmxResponseException;

/**
 * Source of features for SDMX Dimensions
 * 
 * @author lmorandini
 *
 */
public class SDMXDimensionFeatureSource extends SDMXFeatureSource {

  // FIXME:
  protected CoordinateReferenceSystem crs;

  protected String dimName;

  /**
   * Constructor
   * 
   * @param entry
   *          ContentEntry of the feature type
   * @param dataflowIn
   *          SDMX Dataflow the query works on
   * @param query
   *          Query that defines the feature source
   * @throws IOException
   * @throws FactoryException
   */
  public SDMXDimensionFeatureSource(ContentEntry entry, Dataflow dataflowIn,
      String dimNameIn, Query query) throws IOException, FactoryException {

    super(entry, dataflowIn, query);
    this.dimName = dimNameIn;
  }

  @Override
  protected SimpleFeatureType buildFeatureType() throws IOException {

    // Builds the feature type
    SimpleFeatureTypeBuilder builder = this.buildBuilder();
    builder.add(SDMXDataStore.CODE_KEY, java.lang.String.class);
    builder.add(SDMXDataStore.VALUE_KEY, java.lang.String.class);
    this.schema = builder.buildFeatureType();
    return this.schema;
  }

  @Override
  protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
      Query query) throws IOException {

    if (this.schema == null) {
      this.buildFeatureType();  
    }

    try {
      return new SDMXDimensionFeatureReader(this.dataStore.getSDMXClient(),
          this.schema, this.dataflow, this.dataflowStructure, this.dimName,
          this.dataStore.getLogger());
    } catch (SdmxException e) {
      // FIXME: re-hash the exception into an IOEXception
      this.dataStore.getLogger().log(Level.SEVERE, e.getMessage(), e);
      throw new IOException(e);
    }
  }
}
