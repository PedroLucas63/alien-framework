package br.ufrn.dimap.types;

import java.util.Optional;

public enum HttpHeader {
    WWWAuthenticate("WWW-Authenticate"),
    Authorization("Authorization"),
    ProxyAuthenticate("Proxy-Authenticate"),
    ProxyAuthorization("Proxy-Authorization"),

    Age("Age"),
    CacheControl("Cache-Control"),
    ClearSiteData("Clear-Site-Data"),
    Expires("Expires"),

    LastModified("Last-Modified"),
    ETag("ETag"),
    IfNoneMatch("If-None-Match"),
    IfModifiedSince("If-Modified-Since"),
    IfUnmodifiedSince("If-Unmodified-Since"),
    Vary("Vary"),

    Connection("Connection"),
    KeepAlive("Keep-Alive"),

    Accept("Accept"),
    AcceptEncoding("Accept-Encoding"),
    AcceptLanguage("Accept-Language"),
    AcceptPatch("Accept-Patch"),
    AcceptPost("Accept-Post"),
    AcceptQuery("Accept-Query"),

    Expect("Expect"),
    MaxForwards("Max-Forwards"),

    Cookie("Cookie"),
    SetCookie("Set-Cookie"),

    AccessControlAllowCredentials("Access-Control-Allow-Credentials"),
    AccessControlAllowHeaders("Access-Control-Allow-Headers"),
    AccessControlAllowMethods("Access-Control-Allow-Methods"),
    AccessControlAllowOrigin("Access-Control-Allow-Origin"),
    AccessControlExposeHeaders("Access-Control-Expose-Headers"),
    AccessControlMaxAge("Access-Control-Max-Age"),
    AccessControlRequestHeaders("Access-Control-Request-Headers"),
    AccessControlRequestMethod("Access-Control-Request-Method"),
    Origin("Origin"),
    TimingAllowOrigin("Timing-Allow-Origin"),

    ContentDisposition("Content-Disposition"),

    IntegrityPolicy("Integrity-Policy"),
    IntegrityPolicyReportOnly("Integrity-Policy-Report-Only"),

    ContentLength("Content-Length"),
    ContentType("Content-Type"),
    ContentEncoding("Content-Encoding"),
    ContentLanguage("Content-Language"),
    ContentLocation("Content-Location"),

    AcceptSignature("Accept-Signature"),
    Signature("Signature"),
    SignatureInput("Signature-Input"),

    Prefer("Prefer"),
    PreferenceApplied("Preference-Applied"),

    Forward("Forward"),
    Via("Via"),

    AcceptRanges("Accept-Ranges"),
    Range("Range"),
    IfRange("If-Range"),
    ContentRange("Content-Range"),

    Location("Location"),
    Refresh("Refresh"),

    From("From"),
    Host("Host"),
    Referer("Referer"),
    RefererPolicy("Referer-Policy"),
    UserAgent("User-Agent"),

    Allow("Allow"),
    Server("Server"),

    CrossOriginEmbedderPolicy("Cross-Origin-Embedder-Policy"),
    CrossOriginOpenerPolicy("Cross-Origin-Opener-Policy"),
    CrossOriginResourcePolicy("Cross-Origin-Resource-Policy"),
    ContentSecurityPolicy("Content-Security-Policy"),
    ContentSecurityPolicyReportOnly("Content-Security-Policy-Report-Only"),
    ExpectCT("Expect-CT"),
    PermissionsPolicy("Permissions-Policy"),
    StrictTransportSecurity("Strict-Transport-Security"),
    UpgradeInsecureRequest("Upgrade-Insecure-Request"),
    XContentTypeOptions("X-Content-Type-Options"),
    XFrameOptions("X-Frame-Options"),
    XPermittedCrossDomainPolicy("X-Permitted-Cross-Domain-Policy"),
    XPoweredBy("X-Powered-By"),
    XXSSProtection("X-XSS-Protection"),

    SecFetchSite("Sec-Fetch-Site"),
    SecFetchMode("Sec-Fetch-Mode"),
    SecFetchUser("Sec-Fetch-User"),
    SecFetchDest("Sec-Fetch-Dest"),
    SecPurpose("Sec-Purpose"),
    ServiceWorkerNavigationPreload("Service-Worker-Navigation-Preload"),

    SecFetchStorageAccess("Sec-Fetch-Storage-Access"),
    ActivateStorageAccess("Activate-Storage-Access"),

    ReportingEndpoints("Reporting-Endpoints"),
    ReportTo("Report-To"),

    TransferEncoding("Transfer-Encoding"),
    TE("TE"),
    Trailer("Trailer"),

    SecWebSocketAccept("Sec-WebSocket-Accept"),
    SecWebSocketExtensions("Sec-WebSocket-Extensions"),
    SecWebSocketKey("Sec-WebSocket-Key"),
    SecWebSocketProtocol("Sec-WebSocket-Protocol"),
    SecWebSocketVersion("Sec-WebSocket-Version"),

    AltSvc("Alt-Svc"),
    AltUsed("Alt-Used"),
    Date("Date"),
    Link("Link"),
    RetryAfter("Retry-After"),
    ServerTiming("Server-Timing"),
    ServiceWorker("Service-Worker"),
    ServiceWorkerAllowed("Service-Worker-Allowed"),
    SourceMap("Source-Map"),
    Upgrade("Upgrade"),
    Priority("Priority");

    private final String key;

    private HttpHeader(String header) {
        key = header;
    }

    public String getKey() {
        return key;
    }

    public static HttpHeader parse(String header) {
        for (var httpHeader : HttpHeader.values()) {
            if (httpHeader.getKey().equals(header)) {
                return httpHeader;
            }
        }

        throw new IllegalArgumentException("Unknown HTTP header: " + header);
    }

    public static Optional<HttpHeader> tryParse(String header) {
        for (var httpHeader : HttpHeader.values()) {
            if (httpHeader.getKey().equals(header)) {
                return Optional.of(httpHeader);
            }
        }

        return Optional.empty();
    }
}
