package com.usian.filter;

import com.google.common.util.concurrent.RateLimiter;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

@Component
public class RateLimitFilter extends ZuulFilter {

    private static final RateLimiter RATE_LIMIT = RateLimiter.create(1);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }
    //使用令牌桶算法限流
    @Override
    public Object run() throws ZuulException {
        if (!RATE_LIMIT.tryAcquire()) {
            RequestContext requestContext = RequestContext.getCurrentContext();
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseBody(JsonUtils.objectToJson(
                    Result.ok("你小子不要猴急，访问太多频繁，每秒钟只能一次，请稍后再访问！！！")));
            requestContext.getResponse().setContentType(
                    "application/json; charset=utf-8");
        }
        return null;
    }
}
