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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author Karl Heinz Marbaiase
 *
 */
public class GetPrerequisites
{
    /**
     * Currently hard code should be somehow extracted from the configuration file....
     */
    public String[] pluginNames = { 
        "maven-acr-plugin", 
        "maven-ant-plugin", 
        "maven-antrun-plugin",
        "maven-assembly-plugin",
        "maven-changelog-plugin",
        "maven-changes-plugin",
        "maven-checkstyle-plugin",
        "maven-clean-plugin",
        "maven-compiler-plugin",
        "maven-dependency-plugin",
        "maven-deploy-plugin",
        "maven-doap-plugin",
        "maven-docck-plugin",
        "maven-ear-plugin",
        "maven-eclipse-plugin",
        "maven-ejb-plugin",
        "maven-gpg-plugin",
        "maven-help-plugin",
        "maven-install-plugin",
        "maven-invoker-plugin",
        "maven-jar-plugin",
        "maven-jarsigner-plugin",
        "maven-javadoc-plugin",
        "maven-linkcheck-plugin",
        "maven-patch-plugin",
        "maven-pdf-plugin",
        "maven-pmd-plugin",
        "maven-project-info-reports-plugin",
        "maven-rar-plugin",
        "maven-remote-resources-plugin",
        "maven-repository-plugin",
        "maven-scm-publish-plugin",
        "maven-shade-plugin",
        "maven-site-plugin",
        "maven-source-plugin",
        "maven-stage-plugin",
        "maven-toolchains-plugin",
        "maven-verifier-plugin",
        "maven-war-plugin",
    };

    public String BASEURL = "http://maven.apache.org/plugins/";

    public MavenJDKInformation getMavenJdkInformation( String baseURL, String pluginName )
        throws IOException
    {
        Document doc = Jsoup.connect( baseURL + "/" + pluginName + "/plugin-info.html" ).get();

        Elements select = doc.select( "table.bodyTable" );

        Element tableInfo = select.get( 1 );
        Elements elementsByAttribute_a = tableInfo.getElementsByAttributeValue( "class", "a" );
        Elements elementsByAttribute_b = tableInfo.getElementsByAttributeValue( "class", "b" );
        String mavenVersion = elementsByAttribute_a.first().text();
        String jdkVersion = elementsByAttribute_b.first().text();
        
        //FIXME: Sometimes it happens that the indexes are swapped (I don't know why...I have to find out why...)
        if ( mavenVersion.startsWith( "JDK" ) )
        {
            String tmp = jdkVersion;
            jdkVersion = mavenVersion;
            mavenVersion = tmp;
        }

        //Leave only version part...
        mavenVersion = mavenVersion.replace( "Maven ", "" );
        jdkVersion = jdkVersion.replace( "JDK ", "" );

        MavenJDKInformation mjdk = new MavenJDKInformation( pluginName, mavenVersion, jdkVersion );
        return mjdk;
    }

    public List<MavenJDKInformation> getPrequisites()
    {
        List<MavenJDKInformation> result = new ArrayList<MavenJDKInformation>();

        for ( String pluginName : pluginNames )
        {
            try
            {
                result.add( getMavenJdkInformation( BASEURL, pluginName ) );
            }
            catch ( IOException e )
            {
                //What could happen?
                //check it...
            }
        }
        return result;
    }

    public Map<ArtifactVersion, List<MavenJDKInformation>> getGroupedPrequisites()
    {
        Map<ArtifactVersion, List<MavenJDKInformation>> result =
            new HashMap<ArtifactVersion, List<MavenJDKInformation>>();

        List<MavenJDKInformation> prequisites = getPrequisites();
        for ( MavenJDKInformation mavenJDKInformation : prequisites )
        {
            if ( !result.containsKey( mavenJDKInformation.getMavenVersion() ) )
            {
                result.put( mavenJDKInformation.getMavenVersion(), new ArrayList<MavenJDKInformation>() );
            }
            result.get( mavenJDKInformation.getMavenVersion() ).add( mavenJDKInformation );
        }

        return result;
    }
}