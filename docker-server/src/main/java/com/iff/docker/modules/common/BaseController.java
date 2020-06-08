/*******************************************************************************
 * Copyright (c) 2019-11-01 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.modules.common;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * BaseController
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-11-01
 * auto generate by qdp.
 */
public abstract class BaseController {

    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    private static final String[] MOBILE_AGENTS = new String[]{"iphone", "android", "phone", "ipad", "mobile", "wap", "netfront", "java",
            "opera mobi", "opera mini", "ucweb", "windows ce", "symbian", "series", "webos", "sony", "blackberry",
            "dopod", "nokia", "samsung", "palmsource", "xda", "pieplus", "meizu", "midp", "cldc", "motorola",
            "foma", "docomo", "up.browser", "up.link", "blazer", "helio", "hosin", "huawei", "novarra", "coolpad",
            "webos", "techfaith", "palmsource", "alcatel", "amoi", "ktouch", "nexian", "ericsson", "philips",
            "sagem", "wellcom", "bunjalloo", "maui", "smartphone", "iemobile", "spice", "bird", "zte-", "longcos",
            "pantech", "gionee", "portalmmm", "jig browser", "hiptop", "benq", "haier", "^lct", "320x320",
            "240x320", "176x220", "w3c ", "acs-", "alav", "alca", "amoi", "audi", "avan", "benq", "bird", "blac",
            "blaz", "brew", "cell", "cldc", "cmd-", "dang", "doco", "eric", "hipt", "inno", "ipaq", "java", "jigs",
            "kddi", "keji", "leno", "lg-c", "lg-d", "lg-g", "lge-", "maui", "maxo", "midp", "mits", "mmef", "mobi",
            "mot-", "moto", "mwbp", "nec-", "newt", "noki", "oper", "palm", "pana", "pant", "phil", "play", "port",
            "prox", "qwap", "sage", "sams", "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem",
            "smal", "smar", "sony", "sph-", "symb", "t-mo", "teli", "tim-", "tsm-", "upg1", "upsi", "vk-v", "voda",
            "wap-", "wapa", "wapi", "wapp", "wapr", "webc", "winw", "winw", "xda", "xda-", "Googlebot-Mobile"};

    public static String urlDecode(String url) {
        if (url != null && url.length() > 0) {
            try {
                return URLDecoder.decode(url, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static String urlEncode(String url) {
        if (url != null && url.length() > 0) {
            try {
                return URLEncoder.encode(url, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    protected String remoteIp(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
            return request.getHeader("X-Real-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
            return request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("WL-Proxy-Client-IP"))) {
            return request.getHeader("WL-Proxy-Client-IP");
        }
        return request.getRemoteAddr();
    }

    /**
     * 判断是否是手机浏览器
     *
     * @return
     */
    protected boolean isMobile(HttpServletRequest request) {
        boolean isMobile = false;
        // 根据cookie选择模板，暂时未用
        for (Cookie cookie : (Cookie[]) ArrayUtils.nullToEmpty(request.getCookies())) {
            if ("SPARK_THEME".equalsIgnoreCase(cookie.getName())) {
                isMobile = StringUtils.equals("WAP", cookie.getValue());
                break;
            }
        }
        // 根据userAgent自动选择
        String userAgent = StringUtils.defaultString(request.getHeader("User-Agent"), "").toLowerCase();
        if (StringUtils.isBlank(userAgent)) {
            return isMobile;
        }
        for (String mobileAgent : MOBILE_AGENTS) {
            if (userAgent.indexOf(mobileAgent) >= 0) {
                isMobile = true;
                break;
            }
        }
        return isMobile;
    }

    protected ResultBean success() {
        return result(200, SUCCESS, null);
    }

    protected ResultBean success(String message) {
        return result(200, message, null);
    }

    protected ResultBean success(String message, Object data) {
        return result(200, message, data);
    }

    protected ResultBean success(Object data) {
        return result(200, SUCCESS, data);
    }

    protected ResultBean error() {
        return result(500, ERROR, null);
    }

    protected ResultBean error(String message) {
        return result(500, message, null);
    }

    protected ResultBean error(String message, Object data) {
        return result(500, message, data);
    }

    protected ResultBean error(Object data) {
        return result(500, ERROR, data);
    }

    protected ResultBean error(Throwable e) {
        return result(500, e.getMessage(), null);
    }

    protected ResultBean result(int status, String message, Object data) {
        return ResultBean.builder().status(status).message(message).data(data).build();
    }
}
