/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.fuseki.servlets;

import static org.apache.jena.fuseki.HttpNames.paramDefaultGraphURI ;
import static org.apache.jena.fuseki.HttpNames.paramNamedGraphURI ;

import java.util.Arrays ;
import java.util.Collections ;
import java.util.List ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.servlets.SPARQL_Query.HttpActionQuery ;
import org.openjena.atlas.iterator.Filter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.Lib ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.core.DatasetDescription ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** Support for the SPARQL protocol (SPARQL Query, SPARQL Update)
 */
public  abstract class SPARQL_Protocol extends SPARQL_ServletBase
{
    protected class HttpActionProtocol extends HttpAction {
        DatasetDescription datasetDesc = null ;
        
        public HttpActionProtocol(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response, boolean verbose)
        {
            super(id, dsg, request, response, verbose) ;
        }
    }
    
    protected SPARQL_Protocol(PlainRequestFlag noQueryStringIsOK, boolean verbose_debug)
    {
        super(noQueryStringIsOK, verbose_debug) ;
    }

    protected static String messageForQPE(QueryParseException ex)
    {
        if ( ex.getMessage() != null )
            return ex.getMessage() ;
        if ( ex.getCause() != null )
            return Lib.classShortName(ex.getCause().getClass()) ;
        return null ;
    }

    // These are the operations passed on by SPARQL_ServletBase
//    protected abstract String mapRequestToDataset(String uri) ;
//    protected abstract void perform(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response) ;
//    protected abstract boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response) ;

    
    protected static DatasetDescription getDatasetDescription(HttpActionQuery action)
    {
        List<String> graphURLs = toStrList(action.request.getParameterValues(paramDefaultGraphURI)) ;
        List<String> namedGraphs = toStrList(action.request.getParameterValues(paramNamedGraphURI)) ;
        
        graphURLs = removeEmptyValues(graphURLs) ;
        namedGraphs = removeEmptyValues(namedGraphs) ;
        
        if ( graphURLs.size() == 0 && namedGraphs.size() == 0 )
            return null ;
        return DatasetDescription.create(graphURLs, namedGraphs) ;
    }
    
    protected static DatasetDescription getDatasetDescription(Query query)
    {
        return DatasetDescription.create(query) ;
    }
   
    private static List<String> toStrList(String[] array)
    {
        if ( array == null )
            return Collections.emptyList() ;
        return Arrays.asList(array) ;
    }

    private static List<String> removeEmptyValues(List<String> list)
    {
        return Iter.iter(list).filter(acceptNonEmpty).toList() ;
    }
    
    private static Filter<String> acceptNonEmpty = new Filter<String>(){ 
        @Override
        public boolean accept(String item)
        {
            return item != null && item.length() != 0 ;
        }
    } ;
}

