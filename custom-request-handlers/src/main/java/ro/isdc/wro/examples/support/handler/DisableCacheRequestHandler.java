package ro.isdc.wro.examples.support.handler;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.http.handler.RequestHandler;
import ro.isdc.wro.http.handler.RequestHandlerSupport;
import ro.isdc.wro.model.group.GroupExtractor;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.ResourceType;

/**
 * This RequestHandler will reload the cache only for a specific group.
 */
public class DisableCacheRequestHandler extends RequestHandlerSupport {
    private static final Logger LOG = LoggerFactory.getLogger(DisableCacheRequestHandler.class);
    /**
     * The alias of this {@link RequestHandler} used for configuration.
     */
    public static final String ALIAS = "disableCache";
    private static final String PARAM_DISABLE_CACHE = ALIAS;
    @Inject
    private CacheStrategy<CacheKey, CacheValue> cacheStrategy;
    @Inject
    private GroupExtractor groupExtractor;
    @Inject
    private ReadOnlyContext context;

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final String requestUri = request.getRequestURI();

        final String groupName = groupExtractor.getGroupName(request);
        final ResourceType resourceType = groupExtractor.getResourceType(request);
        final boolean isMinimized = groupExtractor.isMinimized(request);

        final CacheKey cacheKey = new CacheKey(groupName, resourceType, isMinimized);
        LOG.debug("invalidating cacheKey: {}", cacheKey);
        cacheStrategy.put(cacheKey, null);


        final RequestDispatcher dispatcher = request.getRequestDispatcher(requestUri);
        try {
            markAsHandled(request);
            dispatcher.forward(request, response);
        } catch (final ServletException e) {
            throw new IOException(e);
        }
    }

    private void markAsHandled(final HttpServletRequest request) {
        request.setAttribute(DisableCacheRequestHandler.class.getName(), true);
    }


    private boolean wasHandled(final HttpServletRequest request) {
        return request.getAttribute(DisableCacheRequestHandler.class.getName()) != null;
    }

    @Override
    public boolean accept(final HttpServletRequest request) {
        final String disableCacheAsString = request.getParameter(PARAM_DISABLE_CACHE);
        return BooleanUtils.toBoolean(disableCacheAsString) && !wasHandled(request);
    }

    @Override
    public boolean isEnabled() {
        return context.getConfig().isDebug();
    }
}
