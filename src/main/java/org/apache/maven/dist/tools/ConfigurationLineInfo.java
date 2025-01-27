package org.apache.maven.dist.tools;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.dist.tools.index.DistCheckIndexPageMojo;

/**
 *
 * @author skygo
 */
public class ConfigurationLineInfo
{
    private static final String URLSEP = "/";

    private final String directory;
    private final String groupId;
    private final boolean srcBin;
    private final String groupIndexPageUrl;

    private final String artifactId;
    private final VersionRange versionRange;
    
    private String forceVersion;
    private String indexPageUrl;
    private Metadata metadata;

    public ConfigurationLineInfo( String[] infos )
    {
        this.directory = infos[0].replace( '/', ' ' ).replace( ':', ' ' ).trim();
        String g = infos[1];
        int index = g.indexOf( ':' );
        this.groupId = ( index < 0 ) ? g : g.substring( 0, index );
        this.srcBin = ( infos.length > 2 ) && "src+bin".equals( infos[2] );
        this.groupIndexPageUrl = ( !srcBin && ( infos.length > 2 ) ) ? infos[2] : null;

        this.artifactId = ( index < 0 ) ? null : g.substring( index + 1 );
        this.versionRange = null;
        this.indexPageUrl = DistCheckIndexPageMojo.POMS_INDEX_URL; // in case of group parent pom artifact
    }

    public ConfigurationLineInfo( ConfigurationLineInfo group, String[] infos )
        throws InvalidVersionSpecificationException
    {
        this.directory = group.getDirectory();
        this.groupId = group.getGroupId();
        this.srcBin = group.isSrcBin();
        this.groupIndexPageUrl = group.groupIndexPageUrl;

        this.artifactId = infos[0];
        this.versionRange = ( infos.length > 1 ) ? VersionRange.createFromVersionSpec( infos[1] ) : null;
        this.indexPageUrl = group.groupIndexPageUrl;
    }

    public String getIndexPageUrl()
    {
        return indexPageUrl;
    }
    
    public String getForcedVersion()
    {
        return forceVersion;
    }
    
    public void setForceVersion( String forceVersion )
    {
        this.forceVersion = forceVersion;
    }
    
    public VersionRange getVersionRange()
    {
        return versionRange;
    }

    /**
     * @return the groupId
     */
    public String getGroupId()
    {
        return groupId;
    }

    /**
     * @return the artifactId
     */
    public String getArtifactId()
    {
        return artifactId;
    }

    /**
     * @return the directory
     */
    public String getDirectory()
    {
        return directory;
    }

    public boolean isSrcBin()
    {
        return srcBin;
    }

    public String getBaseURL( String repoBaseUrl, String folder )
    {
        return repoBaseUrl + groupId.replaceAll( "\\.", URLSEP ) + URLSEP + artifactId + URLSEP + folder;
    }

    public String getMetadataFileURL( String repoBaseUrl )
    {
        return getBaseURL( repoBaseUrl, "maven-metadata.xml" );
    }

    public String getVersionnedFolderURL( String repoBaseUrl, String version )
    {
        return getBaseURL( repoBaseUrl, version ) + '/';
    }

    String getVersionnedPomFileURL( String repoBaseUrl, String version )
    {
        return getBaseURL( repoBaseUrl, version + URLSEP + artifactId + "-" + version + ".pom" );
    }

    void setMetadata( Metadata aMetadata )
    {
        this.metadata = aMetadata;
    }

    public String getReleaseDateFromMetadata()
    {
        try
        {
            SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyyMMddkkmmss" );
            Date f = dateFormatter.parse( metadata.getVersioning().getLastUpdated() );
            // inverted for index page check
            SimpleDateFormat dateFormattertarget = new SimpleDateFormat( "yyyy-MM-dd" );
            return dateFormattertarget.format( f );
        }
        catch ( ParseException ex )
        {
            return "Cannot parse";
        }

    }

    public String getSourceReleaseFilename( String version, boolean dist )
    {
        return artifactId + "-" + version
            + ( srcBin && ( dist || !"maven-ant-tasks".equals( artifactId ) ) ? "-src" : "-source-release" ) + ".zip";
    }
}
