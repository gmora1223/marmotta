/**
 * Copyright (C) 2013 Salzburg Research.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kiwi.core.model.user;

import org.apache.marmotta.commons.sesame.model.Namespaces;
import at.newmedialab.sesame.facading.annotations.RDF;
import at.newmedialab.sesame.facading.annotations.RDFType;
import at.newmedialab.sesame.facading.model.Facade;
import org.openrdf.model.URI;

import java.util.Set;


/**
 * User: Stephanie Stroka
 * Date: 18.05.2011
 * Time: 11:29:17
 */
@RDFType(Namespaces.NS_FOAF + "Person")
public interface KiWiUser extends Facade {

    @RDF(Namespaces.NS_FOAF + "nick")
    public String getNick();
    public void setNick(String nick);

    /**
     * The first name of the user; mapped to the foaf:firstName RDF property
     */
    @RDF(Namespaces.NS_FOAF + "firstName")
    public String getFirstName();
    public void setFirstName(String firstName);

    /**
     * The last name of the user; mapped to the foaf:lastName RDF property
     */
    @RDF(Namespaces.NS_FOAF + "lastName")
    public String getLastName();
    public void setLastName(String lastName);

    @RDF(Namespaces.NS_FOAF + "mbox")
    public String getMbox();
    public void setMbox(String mbox);

    @RDF(Namespaces.NS_FOAF + "depiction")
    public URI getDepiciton();
    public void setDepiction(URI depiction);

    @RDF(Namespaces.NS_FOAF + "account")
    public Set<OnlineAccount> getOnlineAccounts();
    public void setOnlineAccounts(Set<OnlineAccount> onlineAccounts);
    
}
