package vw.be.restapi;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.impl.Utils;
import vw.be.restapi.eventbus.HTTPRequestOverEB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An endpoint holds configuration for a single rest resource - the method, the path and what should be done when
 * called.
 * <p>
 * This class has been taken and adapted from {@link io.vertx.ext.web.impl.RouteImpl}. It is simplified, mainly keeping
 * the same path handling definitions as of the vertx router api.
 */
final class Endpoint {
    private static final Pattern RE_OPERATORS_NO_STAR = Pattern.compile("([\\(\\)\\$\\+\\.])");
    private final HttpMethod method;
    private final String path;
    private final Handler<Message<HTTPRequestOverEB>> handler;
    private Pattern pattern;
    private List<String> groups;
    private boolean exactPath;

    /**
     * Instantiates a new Endpoint.
     *
     * @param method  get, post, etc.
     * @param path    / will match the api root, /:id would then work the same as with the router api; /* is also
     *                supported
     * @param handler what to do when matched
     */
    Endpoint(HttpMethod method, String path, Handler<Message<HTTPRequestOverEB>> handler) {
        this.method = method;
        this.handler = handler;
        checkPath(path);
        this.path = setPath(path);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Endpoint endpoint = (Endpoint) o;

        if (method != endpoint.method) return false;
        return path.equals(endpoint.path);
    }

    @Override public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Endpoint[ ");
        sb.append("path:").append(path);
        sb.append(" pattern:").append(pattern);
        sb.append(" handler:").append(handler);
        sb.append(" method:").append(method);
        sb.append("]@").append(System.identityHashCode(this));
        return sb.toString();
    }

    /**
     * To be called when an endpoint has been matched against a request event.
     *
     * @param event
     */
    public void handle(Message<HTTPRequestOverEB> event) {
        handler.handle(event);
    }

    /**
     * Checks if the parameterised path and method match to the request event. This method will extract and save any
     * path parameters to the query params of the request.
     *
     * @param event       the event
     * @param restApiName the rest api name
     * @return the boolean
     */
    public boolean matches(Message<HTTPRequestOverEB> event, String restApiName) {
        HTTPRequestOverEB request = event.body();
        if (!method.equals(request.getMethod())) {
            return false;
        }
        if (path != null && pattern == null && !pathMatches(restApiName, event)) {
            return false;
        }
        if (pattern != null) {
            String path = Utils.normalizePath(request.getUri());
            if (restApiName != null) {
                path = path.substring(restApiName.length());
            }
            JsonObject query = request.getQuery();
            Matcher m = pattern.matcher(path);
            if (m.matches()) {
                if (m.groupCount() > 0) {
                    Map<String, String> params = new HashMap<>(m.groupCount());
                    if (groups != null) {
                        // Pattern - named params
                        // decode the path as it could contain escaped chars.
                        for (int i = 0; i < groups.size(); i++) {
                            final String k = groups.get(i);
                            final String value = Utils.urlDecode(m.group("p" + i), false);
                            if (!query.containsKey(k)) {
                                params.put(k, value);
                            } else {
                                query.put(k, value);
                            }
                        }
                    } else {
                        // Straight regex - un-named params
                        // decode the path as it could contain escaped chars.
                        for (int i = 0; i < m.groupCount(); i++) {
                            String group = m.group(i + 1);
                            if (group != null) {
                                final String k = "param" + i;
                                final String value = Utils.urlDecode(group, false);
                                if (!query.containsKey(k)) {
                                    params.put(k, value);
                                } else {
                                    query.put(k, value);
                                }
                            }
                        }
                    }
                    for (Map.Entry<String, String> paramEntry : params.entrySet())
                        query.put(paramEntry.getKey(), paramEntry.getValue());
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean pathMatches(String restApiName, Message<HTTPRequestOverEB> event) {
        String thePath = restApiName == null ? path : restApiName + path;
        String requestPath;
        HTTPRequestOverEB request = event.body();

        requestPath = Utils.normalizePath(request.getUri());

        if (exactPath) {
            return pathMatchesExact(requestPath, thePath);
        } else {
            if (thePath.endsWith("/") && requestPath.equals(removeTrailing(thePath))) {
                return true;
            }
            return requestPath.startsWith(thePath);
        }
    }

    private boolean pathMatchesExact(String path1, String path2) {
        // Ignore trailing slash when matching paths
        return removeTrailing(path1).equals(removeTrailing(path2));
    }

    private String removeTrailing(String path) {
        int i = path.length();
        if (path.charAt(i - 1) == '/') {
            path = path.substring(0, i - 1);
        }
        return path;
    }

    private String setPath(String path) {
        // See if the path contains ":" - if so then it contains parameter capture groups and we have to generate
        // a regex for that
        if (path.indexOf(':') != -1) {
            createPatternRegex(path);
            return path;
        } else {
            if (path.charAt(path.length() - 1) != '*') {
                exactPath = true;
                return path;
            } else {
                exactPath = false;
                return path.substring(0, path.length() - 1);
            }
        }
    }

    private void createPatternRegex(String path) {
        // escape path from any regex special chars
        path = RE_OPERATORS_NO_STAR.matcher(path).replaceAll("\\\\$1");
        // allow usage of * at the end as per documentation
        if (path.charAt(path.length() - 1) == '*') {
            path = path.substring(0, path.length() - 1) + ".*";
        }
        // We need to search for any :<token name> tokens in the String and replace them with named capture groups
        Matcher m = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(path);
        StringBuffer sb = new StringBuffer();
        groups = new ArrayList<>();
        int index = 0;
        while (m.find()) {
            String param = "p" + index;
            String group = m.group().substring(1);
            if (groups.contains(group)) {
                throw new IllegalArgumentException("Cannot use identifier " +
                                                   group +
                                                   " more than once in pattern string");
            }
            m.appendReplacement(sb, "(?<" + param + ">[^/]+)");
            groups.add(group);
            index++;
        }
        m.appendTail(sb);
        path = sb.toString();
        pattern = Pattern.compile(path);
    }

    private void checkPath(String path) {
        if ("".equals(path) || path.charAt(0) != '/') {
            throw new IllegalArgumentException("Path must start with /");
        }
    }
}
