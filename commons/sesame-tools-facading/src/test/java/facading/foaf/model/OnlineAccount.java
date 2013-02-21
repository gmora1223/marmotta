/**
 *  Copyright (c) 2012 Salzburg Research.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package facading.foaf.model;


import at.newmedialab.sesame.facading.annotations.RDF;
import at.newmedialab.sesame.facading.annotations.RDFInverse;
import at.newmedialab.sesame.facading.annotations.RDFType;
import at.newmedialab.sesame.facading.model.Facade;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.marmotta.commons.sesame.model.Namespaces;

/**
 * @author Stephanie Stroka
 * User: Stephanie Stroka
 * Date: 18.05.2011
 * Time: 11:29:17
 */
@RDFType(Namespaces.NS_FOAF + "OnlineAccount")
public interface OnlineAccount extends Facade {

    @RDF(Namespaces.NS_FOAF + "accountServiceHomepage")
    public String getAccountServiceHomepage();
    public void setAccountServiceHomepage(String accountServiceHomepage);

    @RDF(Namespaces.NS_FOAF + "accountName")
    public String getAccountName();
    public void setAccountName(String accountName);

    @RDFInverse(Namespaces.NS_FOAF + "account")
    public Person getHolder();
    public void setHolder(Person holder);

    @RDF(Namespaces.NS_FOAF + "chatId")
    public Set<String> getChatId();
    public void addChatId(String chatId);

    @RDF("http://www.example.com/rdf/vocab/login")
    public List<Date> getLoginDate();
    public void addLoginDate(Date login);
    public void setLoginDate(List<Date> logins);
}
