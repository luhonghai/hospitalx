/**********************************************************************
 Copyright (c) 2009 Erik Bengtson and others. All rights reserved.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.


 Contributors:
 ...
 **********************************************************************/
package org.datanucleus.api.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ExecutionContext;
import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManager;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.exceptions.ClassNotResolvedException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.identity.OID;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.util.NucleusLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This servlet exposes persistent class via RESTful HTTP requests.
 * Supports the following
 * <ul>
 * <li>GET (retrieve/query)</li>
 * <li>POST (update/insert)</li>
 * <li>PUT (update/insert)</li>
 * <li>DELETE (delete)</li>
 * <li>HEAD (validate)</li>
 * </ul>
 */
public class CustomRestServlet extends HttpServlet
{
    public static final NucleusLogger LOGGER_REST = NucleusLogger.getLoggerInstance("DataNucleus.REST");

    PersistenceManagerFactory pmf;
    PersistenceNucleusContext nucCtx;

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#destroy()
     */
    public void destroy()
    {
        if (pmf != null && !pmf.isClosed())
        {
            LOGGER_REST.info("REST : Closing PMF");
            pmf.close();
        }
        super.destroy();
    }

    public void init(ServletConfig config) throws ServletException
    {
        String factory = config.getInitParameter("persistence-context");
        if (factory == null)
        {
            throw new ServletException("You haven't specified \"persistence-context\" property defining the persistence unit");
        }

        try
        {
            LOGGER_REST.info("REST : Creating PMF for factory=" + factory);
            pmf = JDOHelper.getPersistenceManagerFactory(factory);
            this.nucCtx = ((JDOPersistenceManagerFactory)pmf).getNucleusContext();
        }
        catch (Exception e)
        {
            LOGGER_REST.error("Exception creating PMF", e);
            throw new ServletException("Could not create internal PMF. See nested exception for details", e);
        }

        super.init(config);
    }

    /**
     * Convenience method to get the next token after a "/".
     * @param req The request
     * @return The next token
     */
    private String getNextTokenAfterSlash(HttpServletRequest req)
    {
        String path = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length());
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        return tokenizer.nextToken();
    }

    /**
     * Convenience accessor to get the id, following a "/".
     * @param req The request
     * @return The id (or null if no slash)
     */
    private Object getId(HttpServletRequest req)
    {
        ClassLoaderResolver clr = nucCtx.getClassLoaderResolver(CustomRestServlet.class.getClassLoader());
        String path = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length());
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        String className = tokenizer.nextToken();
        AbstractClassMetaData cmd = nucCtx.getMetaDataManager().getMetaDataForClass(className, clr);

        String id = null;
        if (tokenizer.hasMoreTokens())
        {
            // "id" single-field specified in URL
            id = tokenizer.nextToken();
            if (id == null || cmd == null)
            {
                return null;
            }

            Object identity = RESTUtils.getIdentityForURLToken(cmd, id, nucCtx);
            if (identity != null)
            {
                return identity;
            }
        }

        // "id" must have been specified in the content of the request
        try
        {
            if (id == null && req.getContentLength() > 0)
            {
                char[] buffer = new char[req.getContentLength()];
                req.getReader().read(buffer);
                id = new String(buffer);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        if (id == null || cmd == null)
        {
            return null;
        }

        try
        {
            // assume it's a JSONObject
            id = URLDecoder.decode(id, "UTF-8");
            JSONObject jsonobj = new JSONObject(id);
            return RESTUtils.getNonPersistableObjectFromJSONObject(jsonobj, clr.classForName(cmd.getObjectidClass()), nucCtx);
        }
        catch (JSONException ex)
        {
            // not JSON syntax
        }
        catch (UnsupportedEncodingException e)
        {
            LOGGER_REST.error("Exception caught when trying to determine id", e);
        }

        return id;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        // Retrieve any fetch group that needs applying to the fetch
        String fetchParam = req.getParameter("fetch");
        String page = req.getParameter("page");
        String start = req.getParameter("start");
        String limit = req.getParameter("limit");
        long mPage = -1, mStart = -1, mLimit = -1;

        try
        {
            if (page != null && page.length() > 0) {
                mPage = Long.parseLong(page);
            }
            if (start != null && start.length() > 0) {
                mStart = Long.parseLong(start);
            }
            if (limit != null && limit.length() > 0) {
                mLimit = Long.parseLong(limit);
            }

            String token = getNextTokenAfterSlash(req);
            LOGGER_REST.info("token:" + token);
            if (mPage != -1 && mStart != -1 && mLimit != -1) {
                // GET "/{candidateclass}..."
                String className = token;
                ClassLoaderResolver clr = nucCtx.getClassLoaderResolver(CustomRestServlet.class.getClassLoader());
                AbstractClassMetaData cmd = nucCtx.getMetaDataManager().getMetaDataForEntityName(className);
                try
                {
                    if (cmd == null)
                    {
                        cmd = nucCtx.getMetaDataManager().getMetaDataForClass(className, clr);
                    }
                }
                catch (ClassNotResolvedException ex)
                {
                    JSONObject error = new JSONObject();
                    error.put("exception", ex.getMessage());
                    resp.getWriter().write(error.toString());
                    resp.setStatus(404);
                    resp.setHeader("Content-Type", "application/json");
                    return;
                }

                // Find objects by type or by query
                try
                {
                    // get the whole extent for this candidate
                    String queryString = "SELECT FROM " + cmd.getFullClassName();

                    PersistenceManager pm = pmf.getPersistenceManager();
                    if (fetchParam != null)
                    {
                        pm.getFetchPlan().addGroup(fetchParam);
                    }
                    try
                    {
                        pm.currentTransaction().begin();
                        Query query = pm.newQuery("JDOQL", queryString);
                        query.setRange(mStart, mStart + mLimit);
                        List result = (List)query.execute();
                        JSONArray jsonobj = RESTUtils.getJSONArrayFromCollection(result,
                                ((JDOPersistenceManager)pm).getExecutionContext());
                        resp.getWriter().write(jsonobj.toString());
                        resp.setHeader("Content-Type", "application/json");
                        resp.setStatus(200);
                        pm.currentTransaction().commit();
                    }
                    finally
                    {
                        if (pm.currentTransaction().isActive())
                        {
                            pm.currentTransaction().rollback();
                        }
                        pm.close();
                    }
                    return;
                }
                catch (NucleusUserException e)
                {
                    JSONObject error = new JSONObject();
                    error.put("exception", e.getMessage());
                    resp.getWriter().write(error.toString());
                    resp.setStatus(400);
                    resp.setHeader("Content-Type", "application/json");
                    return;
                }
                catch (NucleusException ex)
                {
                    JSONObject error = new JSONObject();
                    error.put("exception", ex.getMessage());
                    resp.getWriter().write(error.toString());
                    resp.setStatus(404);
                    resp.setHeader("Content-Type", "application/json");
                    return;
                }
                catch (RuntimeException ex)
                {
                    // errors from the google appengine may be raised when running queries
                    JSONObject error = new JSONObject();
                    error.put("exception", ex.getMessage());
                    resp.getWriter().write(error.toString());
                    resp.setStatus(404);
                    resp.setHeader("Content-Type", "application/json");
                    return;
                }

            }
            else if (token.equalsIgnoreCase("query") || token.equalsIgnoreCase("jdoql"))
            {
                // GET "/query?the_query_details" or GET "/jdoql?the_query_details" where "the_query_details" is "SELECT FROM ... WHERE ... ORDER BY ..."
                String queryString = URLDecoder.decode(req.getQueryString(), "UTF-8");
                PersistenceManager pm = pmf.getPersistenceManager();
                try
                {
                    pm.currentTransaction().begin();

                    Query query = pm.newQuery("JDOQL", queryString);
                    if (fetchParam != null)
                    {
                        query.getFetchPlan().addGroup(fetchParam);
                    }
                    Object result = query.execute();
                    if (result instanceof Collection)
                    {
                        JSONArray jsonobj = RESTUtils.getJSONArrayFromCollection((Collection)result,
                                ((JDOPersistenceManager)pm).getExecutionContext());
                        resp.getWriter().write(jsonobj.toString());
                    }
                    else
                    {
                        JSONObject jsonobj = RESTUtils.getJSONObjectFromPOJO(result,
                                ((JDOPersistenceManager)pm).getExecutionContext());
                        resp.getWriter().write(jsonobj.toString());
                    }
                    resp.setHeader("Content-Type", "application/json");
                    resp.setStatus(200);
                    pm.currentTransaction().commit();
                }
                finally
                {
                    if (pm.currentTransaction().isActive())
                    {
                        pm.currentTransaction().rollback();
                    }
                    pm.close();
                }
                return;
            }
            else if (token.equalsIgnoreCase("jpql"))
            {
                // GET "/jpql?the_query_details" where "the_query_details" is "SELECT ... FROM ... WHERE ... ORDER BY ..."
                String queryString = URLDecoder.decode(req.getQueryString(), "UTF-8");
                PersistenceManager pm = pmf.getPersistenceManager();
                try
                {
                    pm.currentTransaction().begin();
                    Query query = pm.newQuery("JPQL", queryString);
                    if (fetchParam != null)
                    {
                        query.getFetchPlan().addGroup(fetchParam);
                    }
                    Object result = query.execute();
                    if (result instanceof Collection)
                    {
                        JSONArray jsonobj = RESTUtils.getJSONArrayFromCollection((Collection)result,
                                ((JDOPersistenceManager)pm).getExecutionContext());
                        resp.getWriter().write(jsonobj.toString());
                    }
                    else
                    {
                        JSONObject jsonobj = RESTUtils.getJSONObjectFromPOJO(result,
                                ((JDOPersistenceManager)pm).getExecutionContext());
                        resp.getWriter().write(jsonobj.toString());
                    }
                    resp.setHeader("Content-Type", "application/json");
                    resp.setStatus(200);
                    pm.currentTransaction().commit();
                }
                finally
                {
                    if (pm.currentTransaction().isActive())
                    {
                        pm.currentTransaction().rollback();
                    }
                    pm.close();
                }
                return;
            }
            else
            {
                // GET "/{candidateclass}..."
                String className = token;
                ClassLoaderResolver clr = nucCtx.getClassLoaderResolver(CustomRestServlet.class.getClassLoader());
                AbstractClassMetaData cmd = nucCtx.getMetaDataManager().getMetaDataForEntityName(className);
                try
                {
                    if (cmd == null)
                    {
                        cmd = nucCtx.getMetaDataManager().getMetaDataForClass(className, clr);
                    }
                }
                catch (ClassNotResolvedException ex)
                {
                    JSONObject error = new JSONObject();
                    error.put("exception", ex.getMessage());
                    resp.getWriter().write(error.toString());
                    resp.setStatus(404);
                    resp.setHeader("Content-Type", "application/json");
                    return;
                }

                Object id = getId(req);
                if (id == null)
                {
                    // Find objects by type or by query
                    try
                    {
                        // get the whole extent for this candidate
                        String queryString = "SELECT FROM " + cmd.getFullClassName();
                        if (req.getQueryString() != null)
                        {
                            // query by filter for this candidate
                            queryString += " WHERE " + URLDecoder.decode(req.getQueryString(), "UTF-8");
                        }
                        PersistenceManager pm = pmf.getPersistenceManager();
                        if (fetchParam != null)
                        {
                            pm.getFetchPlan().addGroup(fetchParam);
                        }
                        try
                        {
                            pm.currentTransaction().begin();
                            Query query = pm.newQuery("JDOQL", queryString);
                            List result = (List)query.execute();
                            JSONArray jsonobj = RESTUtils.getJSONArrayFromCollection(result,
                                    ((JDOPersistenceManager)pm).getExecutionContext());
                            resp.getWriter().write(jsonobj.toString());
                            resp.setHeader("Content-Type", "application/json");
                            resp.setStatus(200);
                            pm.currentTransaction().commit();
                        }
                        finally
                        {
                            if (pm.currentTransaction().isActive())
                            {
                                pm.currentTransaction().rollback();
                            }
                            pm.close();
                        }
                        return;
                    }
                    catch (NucleusUserException e)
                    {
                        JSONObject error = new JSONObject();
                        error.put("exception", e.getMessage());
                        resp.getWriter().write(error.toString());
                        resp.setStatus(400);
                        resp.setHeader("Content-Type", "application/json");
                        return;
                    }
                    catch (NucleusException ex)
                    {
                        JSONObject error = new JSONObject();
                        error.put("exception", ex.getMessage());
                        resp.getWriter().write(error.toString());
                        resp.setStatus(404);
                        resp.setHeader("Content-Type", "application/json");
                        return;
                    }
                    catch (RuntimeException ex)
                    {
                        // errors from the google appengine may be raised when running queries
                        JSONObject error = new JSONObject();
                        error.put("exception", ex.getMessage());
                        resp.getWriter().write(error.toString());
                        resp.setStatus(404);
                        resp.setHeader("Content-Type", "application/json");
                        return;
                    }
                }
                else
                {
                    // GET "/{candidateclass}/id" - Find object by id
                    PersistenceManager pm = pmf.getPersistenceManager();
                    if (fetchParam != null)
                    {
                        pm.getFetchPlan().addGroup(fetchParam);
                    }
                    try
                    {
                        pm.currentTransaction().begin();
                        Object result = pm.getObjectById(id);
                        JSONObject jsonobj = RESTUtils.getJSONObjectFromPOJO(result,
                                ((JDOPersistenceManager)pm).getExecutionContext());
                        resp.getWriter().write(jsonobj.toString());
                        resp.setHeader("Content-Type","application/json");
                        pm.currentTransaction().commit();
                        return;
                    }
                    catch (NucleusObjectNotFoundException ex)
                    {
                        resp.setContentLength(0);
                        resp.setStatus(404);
                        return;
                    }
                    catch (NucleusException ex)
                    {
                        JSONObject error = new JSONObject();
                        error.put("exception", ex.getMessage());
                        resp.getWriter().write(error.toString());
                        resp.setStatus(404);
                        resp.setHeader("Content-Type", "application/json");
                        return;
                    }
                    finally
                    {
                        if (pm.currentTransaction().isActive())
                        {
                            pm.currentTransaction().rollback();
                        }
                        pm.close();
                    }
                }
            }
        }
        catch (JSONException e)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("exception", e.getMessage());
                resp.getWriter().write(error.toString());
                resp.setStatus(404);
                resp.setHeader("Content-Type", "application/json");
            }
            catch (JSONException e1)
            {
                // ignore
            }
        }
    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        resp.addHeader("Allow", " GET, HEAD, POST, PUT, TRACE, OPTIONS");
        resp.setContentLength(0);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        if (req.getContentLength() < 1)
        {
            resp.setContentLength(0);
            resp.setStatus(400);// bad request
            return;
        }

        char[] buffer = new char[req.getContentLength()];
        req.getReader().read(buffer);
        String str = new String(buffer);
        JSONObject jsonobj;
        PersistenceManager pm = pmf.getPersistenceManager();
        ExecutionContext ec = ((JDOPersistenceManager)pm).getExecutionContext();
        try
        {
            pm.currentTransaction().begin();
            jsonobj = new JSONObject(str);
            String className = getNextTokenAfterSlash(req);
            jsonobj.put("class", className);

            // Process any id info provided in the URL
            AbstractClassMetaData cmd = ec.getMetaDataManager().getMetaDataForClass(className, ec.getClassLoaderResolver());
            String path = req.getRequestURI().substring(req.getContextPath().length() + req.getServletPath().length());
            StringTokenizer tokenizer = new StringTokenizer(path, "/");
            tokenizer.nextToken(); // className
            if (tokenizer.hasMoreTokens())
            {
                String idToken = tokenizer.nextToken();
                Object id = RESTUtils.getIdentityForURLToken(cmd, idToken, nucCtx);
                if (id != null)
                {
                    if (cmd.getIdentityType() == IdentityType.APPLICATION)
                    {
                        if (cmd.usesSingleFieldIdentityClass())
                        {
                            jsonobj.put(cmd.getPrimaryKeyMemberNames()[0], ec.getApiAdapter().getTargetKeyForSingleFieldIdentity(id));
                        }
                    }
                    else if (cmd.getIdentityType() == IdentityType.DATASTORE)
                    {
                        jsonobj.put("_id", ((OID)id).getKeyValue());
                    }
                }
            }

            Object pc = RESTUtils.getObjectFromJSONObject(jsonobj, className, ec);
            Object obj = pm.makePersistent(pc);
            JSONObject jsonobj2 = RESTUtils.getJSONObjectFromPOJO(obj, ec);
            resp.getWriter().write(jsonobj2.toString());
            resp.setHeader("Content-Type", "application/json");
            pm.currentTransaction().commit();
        }
        catch (ClassNotResolvedException e)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("exception", e.getMessage());
                resp.getWriter().write(error.toString());
                resp.setStatus(500);
                resp.setHeader("Content-Type", "application/json");
                LOGGER_REST.error(e.getMessage(), e);
            }
            catch (JSONException e1)
            {
                throw new RuntimeException(e1);
            }
        }
        catch (NucleusUserException e)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("exception", e.getMessage());
                resp.getWriter().write(error.toString());
                resp.setStatus(400);
                resp.setHeader("Content-Type", "application/json");
                LOGGER_REST.error(e.getMessage(), e);
            }
            catch (JSONException e1)
            {
                throw new RuntimeException(e1);
            }
        }
        catch (NucleusException e)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("exception", e.getMessage());
                resp.getWriter().write(error.toString());
                resp.setStatus(500);
                resp.setHeader("Content-Type", "application/json");
                LOGGER_REST.error(e.getMessage(), e);
            }
            catch (JSONException e1)
            {
                throw new RuntimeException(e1);
            }
        }
        catch (JSONException e)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("exception", e.getMessage());
                resp.getWriter().write(error.toString());
                resp.setStatus(500);
                resp.setHeader("Content-Type", "application/json");
                LOGGER_REST.error(e.getMessage(), e);
            }
            catch (JSONException e1)
            {
                throw new RuntimeException(e1);
            }
        }
        finally
        {
            if (pm.currentTransaction().isActive())
            {
                pm.currentTransaction().rollback();
            }
            pm.close();
        }
        resp.setStatus(201);// created
    }

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        PersistenceManager pm = pmf.getPersistenceManager();
        try
        {
            String className = getNextTokenAfterSlash(req);
            ClassLoaderResolver clr = nucCtx.getClassLoaderResolver(CustomRestServlet.class.getClassLoader());
            AbstractClassMetaData cmd = nucCtx.getMetaDataManager().getMetaDataForEntityName(className);
            try
            {
                if (cmd == null)
                {
                    cmd = nucCtx.getMetaDataManager().getMetaDataForClass(className, clr);
                }
            }
            catch (ClassNotResolvedException ex)
            {
                try
                {
                    JSONObject error = new JSONObject();
                    error.put("exception", ex.getMessage());
                    resp.getWriter().write(error.toString());
                    resp.setStatus(404);
                    resp.setHeader("Content-Type", "application/json");
                }
                catch (JSONException e)
                {
                    // will not happen
                }
                return;
            }

            Object id = getId(req);
            if (id == null)
            {
                // Delete all objects of this type
                pm.currentTransaction().begin();
                Query q = pm.newQuery("SELECT FROM " + cmd.getFullClassName());
                q.deletePersistentAll();
                pm.currentTransaction().commit();
            }
            else
            {
                // Delete the object with the supplied id
                pm.currentTransaction().begin();
                Object obj = pm.getObjectById(id);
                pm.deletePersistent(obj);
                pm.currentTransaction().commit();
            }
        }
        catch (NucleusObjectNotFoundException ex)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("exception", ex.getMessage());
                resp.getWriter().write(error.toString());
                resp.setStatus(400);
                resp.setHeader("Content-Type", "application/json");
                return;
            }
            catch (JSONException e)
            {
                // will not happen
            }
        }
        catch (NucleusUserException e)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("exception", e.getMessage());
                resp.getWriter().write(error.toString());
                resp.setStatus(400);
                resp.setHeader("Content-Type", "application/json");
                return;
            }
            catch (JSONException e1)
            {
                // ignore
            }
        }
        catch (NucleusException e)
        {
            try
            {
                JSONObject error = new JSONObject();
                error.put("exception", e.getMessage());
                resp.getWriter().write(error.toString());
                resp.setStatus(500);
                resp.setHeader("Content-Type", "application/json");
                LOGGER_REST.error(e.getMessage(), e);
            }
            catch (JSONException e1)
            {
                // ignore
            }
        }
        finally
        {
            if (pm.currentTransaction().isActive())
            {
                pm.currentTransaction().rollback();
            }
            pm.close();
        }
        resp.setContentLength(0);
        resp.setStatus(204);// created
    }

    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String className = getNextTokenAfterSlash(req);
        ClassLoaderResolver clr = nucCtx.getClassLoaderResolver(CustomRestServlet.class.getClassLoader());
        AbstractClassMetaData cmd = nucCtx.getMetaDataManager().getMetaDataForEntityName(className);
        try
        {
            if (cmd == null)
            {
                cmd = nucCtx.getMetaDataManager().getMetaDataForClass(className, clr);
            }
        }
        catch (ClassNotResolvedException ex)
        {
            resp.setStatus(404);
            return;
        }

        Object id = getId(req);
        if (id == null)
        {
            // no id provided
            try
            {
                // get the whole extent
                String queryString = "SELECT FROM " + cmd.getFullClassName();
                if (req.getQueryString() != null)
                {
                    // query by filter
                    queryString += " WHERE " + URLDecoder.decode(req.getQueryString(), "UTF-8");
                }
                PersistenceManager pm = pmf.getPersistenceManager();
                try
                {
                    pm.currentTransaction().begin();
                    Query query = pm.newQuery("JDOQL", queryString);
                    query.execute();
                    resp.setStatus(200);
                    pm.currentTransaction().commit();
                }
                finally
                {
                    if (pm.currentTransaction().isActive())
                    {
                        pm.currentTransaction().rollback();
                    }
                    pm.close();
                }
                return;
            }
            catch (NucleusUserException e)
            {
                resp.setStatus(400);
                return;
            }
            catch (NucleusException ex)
            {
                resp.setStatus(404);
                return;
            }
            catch (RuntimeException ex)
            {
                resp.setStatus(404);
                return;
            }
        }

        PersistenceManager pm = pmf.getPersistenceManager();
        try
        {
            pm.currentTransaction().begin();
            pm.getObjectById(id);
            resp.setStatus(200);
            pm.currentTransaction().commit();
            return;
        }
        catch (NucleusException ex)
        {
            resp.setStatus(404);
            return;
        }
        finally
        {
            if (pm.currentTransaction().isActive())
            {
                pm.currentTransaction().rollback();
            }
            pm.close();
        }
    }
}