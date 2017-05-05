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

  protected final class VisitFilter extends DefaultFilterVisitor {

    public Object visit(Or expr, Object data) {
      Map<String, String> map = (Map<String, String>) data;
      List<String> ids = new ArrayList<String>();
      List<String> property = new ArrayList<String>();

      expr.getChildren().forEach(eqExpr -> {
        property
            .add(((PropertyIsEqualTo) (eqExpr)).getExpression1().toString());
        ids.add(((PropertyIsEqualTo) (eqExpr)).getExpression2().toString());
      });

      map.put(property.get(0),
          String.join(SDMXDataStore.OR_EXP, ((List<String>) ids)));
      return map;
    }

    public Object visit(PropertyIsEqualTo expr, Object data) {
      Map<String, String> map = (Map<String, String>) data;

      map.put(expr.getExpression1().toString(),
          expr.getExpression2().toString());
      return map;
    }

  }

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

    builder.add("the_geom", Point.class);
    builder.setDefaultGeometry(SDMXDataStore.GEOMETRY_ATTR);
    builder.add(SDMXDataStore.TIME_KEY, java.lang.String.class);

    this.dataflowStructure.getDimensions().forEach(dim -> {
      if (SDMXDataStore.MEASURE_KEY.equals(dim.getId().toUpperCase())) {
        dim.getCodeList().getCodes().entrySet().forEach(entry -> {
          builder.add(SDMXDataStore.MEASURE_KEY + SDMXDataStore.SEPARATOR_MEASURE + entry.getKey(),
              java.lang.Double.class);
        });
      } else {
        builder.add(dim.getId(), java.lang.String.class);
      }
    });

    this.schema = builder.buildFeatureType();
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
    // FIXME: I think SDMNX does not support that
    return 1;
  }

  @Override
  protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(
      Query query) throws IOException {

    try {
      return new SDMXFeatureReader(this.dataStore.getSDMXClient(), this.schema,
          this.dataflow, this.dataflowStructure, this.buildConstraints(query),
          this.dataStore.getLogger());
    } catch (SdmxException e) {
      // FIXME: re-hash the exception into an IOEXception
      this.dataStore.getLogger().log(Level.SEVERE, e.getMessage(), e);
      throw new IOException(e);
    }
  }

  /**
   * Builds the SDMX expression to reflect the GeoTools query give as input
   * 
   * @param query
   *          GeoTools query to transform into SDMX constraints
   * @return The SDMX expression
   */
  public String buildConstraints(Query query) throws SdmxException {

    Map<String, String> expressions;
    ArrayList<String> constraints = new ArrayList<String>(
        this.dataflowStructure.getDimensions().size());

    // All-in query
    if (Query.ALL.equals(query)) {
      this.dataflowStructure.getDimensions().forEach(dim -> {
        constraints.add(SDMXDataStore.ALLCODES_EXP);
      });
      // Builds a non-all-in query
    } else {
      expressions = (Map<String, String>) query.getFilter().accept(
          new SDMXFeatureSource.VisitFilter(), new HashMap<String, String>());

      this.dataflowStructure.getDimensions().forEach(dim -> {
        constraints.add((String) expressions.get(dim.getId()));
      });
    }

    return String.join(SDMXDataStore.SEPARATOR_EXP, constraints);
  }
}
