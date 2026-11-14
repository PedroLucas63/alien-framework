package br.ufrn.dimap.factories;

import br.ufrn.dimap.options.HttpMessageOptions;
import br.ufrn.dimap.types.*;

import java.io.InputStream;
import java.util.Map;

public class HttpResponseFactory extends HttpMessageFactory {
    public HttpResponseFactory(HttpMessageOptions options) {
        super(options);
    }

    // 1xx Informational
    public HttpResponse continueStatus() { return create(HttpStatusCode.Continue); }
    public HttpResponse continueStatus(Map<String, String> headers) { return create(HttpStatusCode.Continue, null, null, headers); }

    public HttpResponse switchingProtocols() { return create(HttpStatusCode.SwitchingProtocols); }
    public HttpResponse switchingProtocols(Map<String, String> headers) { return create(HttpStatusCode.SwitchingProtocols, null, null, headers); }

    public HttpResponse processing() { return create(HttpStatusCode.Processing); }
    public HttpResponse processing(Map<String, String> headers) { return create(HttpStatusCode.Processing, null, null, headers); }

    public HttpResponse earlyHints() { return create(HttpStatusCode.EarlyHints); }
    public HttpResponse earlyHints(Map<String, String> headers) { return create(HttpStatusCode.EarlyHints, null, null, headers); }

    // 2xx Success
    public HttpResponse ok() { return create(HttpStatusCode.Ok); }
    public HttpResponse ok(Object content) { return create(HttpStatusCode.Ok, content); }
    public HttpResponse ok(Object content, Map<String, String> headers) { return create(HttpStatusCode.Ok, content, null, headers); }

    public HttpResponse created() { return create(HttpStatusCode.Created); }
    public HttpResponse created(Object content) { return create(HttpStatusCode.Created, content); }
    public HttpResponse created(Object content, String location) { return create(HttpStatusCode.Created, content, location); }
    public HttpResponse created(Object content, String location, Map<String, String> headers) { return create(HttpStatusCode.Created, content, location, headers); }

    public HttpResponse accepted() { return create(HttpStatusCode.Accepted); }
    public HttpResponse accepted(Object content) { return create(HttpStatusCode.Accepted, content); }
    public HttpResponse accepted(Object content, String location) { return create(HttpStatusCode.Accepted, content, location); }
    public HttpResponse accepted(Object content, String location, Map<String, String> headers) { return create(HttpStatusCode.Accepted, content, location, headers); }

    public HttpResponse nonAuthoritativeInformation() { return create(HttpStatusCode.NonAuthoritativeInformation); }
    public HttpResponse nonAuthoritativeInformation(Object content) { return create(HttpStatusCode.NonAuthoritativeInformation, content); }
    public HttpResponse nonAuthoritativeInformation(Object content, Map<String, String> headers) { return create(HttpStatusCode.NonAuthoritativeInformation, content, null, headers); }

    public HttpResponse noContent() { return create(HttpStatusCode.NoContent); }
    public HttpResponse noContent(Map<String, String> headers) { return create(HttpStatusCode.NoContent, null, null, headers); }

    public HttpResponse resetContent() { return create(HttpStatusCode.Reset); }
    public HttpResponse resetContent(Map<String, String> headers) { return create(HttpStatusCode.Reset, null, null, headers); }

    public HttpResponse partialContent(Object content) { return create(HttpStatusCode.PartialContent, content); }
    public HttpResponse partialContent(Object content, Map<String, String> headers) { return create(HttpStatusCode.PartialContent, content, null, headers); }

    public HttpResponse multiStatus(Object content) { return create(HttpStatusCode.MultiStatus, content); }
    public HttpResponse multiStatus(Object content, Map<String, String> headers) { return create(HttpStatusCode.MultiStatus, content, null, headers); }

    public HttpResponse alreadyReported() { return create(HttpStatusCode.AlreadyReported); }
    public HttpResponse alreadyReported(Object content) { return create(HttpStatusCode.AlreadyReported, content); }
    public HttpResponse alreadyReported(Object content, Map<String, String> headers) { return create(HttpStatusCode.AlreadyReported, content, null, headers); }

    public HttpResponse imUsed() { return create(HttpStatusCode.IMUsed); }
    public HttpResponse imUsed(Object content) { return create(HttpStatusCode.IMUsed, content); }
    public HttpResponse imUsed(Object content, Map<String, String> headers) { return create(HttpStatusCode.IMUsed, content, null, headers); }

    // 3xx Redirection
    public HttpResponse multipleChoices() { return create(HttpStatusCode.MultipleChoices); }
    public HttpResponse multipleChoices(Object content) { return create(HttpStatusCode.MultipleChoices, content); }
    public HttpResponse multipleChoices(Object content, String location) { return create(HttpStatusCode.MultipleChoices, content, location); }
    public HttpResponse multipleChoices(Object content, String location, Map<String, String> headers) { return create(HttpStatusCode.MultipleChoices, content, location, headers); }

    public HttpResponse movedPermanently(String location) { return create(HttpStatusCode.MovedPermanently, null, location); }
    public HttpResponse movedPermanently(String location, Object content) { return create(HttpStatusCode.MovedPermanently, content, location); }
    public HttpResponse movedPermanently(String location, Object content, Map<String, String> headers) { return create(HttpStatusCode.MovedPermanently, content, location, headers); }

    public HttpResponse found(String location) { return create(HttpStatusCode.Found, null, location); }
    public HttpResponse found(String location, Object content) { return create(HttpStatusCode.Found, content, location); }
    public HttpResponse found(String location, Object content, Map<String, String> headers) { return create(HttpStatusCode.Found, content, location, headers); }

    public HttpResponse seeOther(String location) { return create(HttpStatusCode.SeeOther, null, location); }
    public HttpResponse seeOther(String location, Object content) { return create(HttpStatusCode.SeeOther, content, location); }
    public HttpResponse seeOther(String location, Object content, Map<String, String> headers) { return create(HttpStatusCode.SeeOther, content, location, headers); }

    public HttpResponse notModified() { return create(HttpStatusCode.NotModified); }
    public HttpResponse notModified(Map<String, String> headers) { return create(HttpStatusCode.NotModified, null, null, headers); }

    public HttpResponse temporaryRedirect(String location) { return create(HttpStatusCode.TemporaryRedirect, null, location); }
    public HttpResponse temporaryRedirect(String location, Object content) { return create(HttpStatusCode.TemporaryRedirect, content, location); }
    public HttpResponse temporaryRedirect(String location, Object content, Map<String, String> headers) { return create(HttpStatusCode.TemporaryRedirect, content, location, headers); }

    public HttpResponse permanentRedirect(String location) { return create(HttpStatusCode.PermanentRedirect, null, location); }
    public HttpResponse permanentRedirect(String location, Object content) { return create(HttpStatusCode.PermanentRedirect, content, location); }
    public HttpResponse permanentRedirect(String location, Object content, Map<String, String> headers) { return create(HttpStatusCode.PermanentRedirect, content, location, headers); }

    // 4xx Client Error
    public HttpResponse badRequest() { return create(HttpStatusCode.BadRequest); }
    public HttpResponse badRequest(Object content) { return create(HttpStatusCode.BadRequest, content); }
    public HttpResponse badRequest(Object content, Map<String, String> headers) { return create(HttpStatusCode.BadRequest, content, null, headers); }

    public HttpResponse unauthorized() { return create(HttpStatusCode.Unauthorized); }
    public HttpResponse unauthorized(Object content) { return create(HttpStatusCode.Unauthorized, content); }
    public HttpResponse unauthorized(Object content, Map<String, String> headers) { return create(HttpStatusCode.Unauthorized, content, null, headers); }

    public HttpResponse paymentRequired() { return create(HttpStatusCode.PaymentRequired); }
    public HttpResponse paymentRequired(Object content) { return create(HttpStatusCode.PaymentRequired, content); }
    public HttpResponse paymentRequired(Object content, Map<String, String> headers) { return create(HttpStatusCode.PaymentRequired, content, null, headers); }

    public HttpResponse forbidden() { return create(HttpStatusCode.Forbidden); }
    public HttpResponse forbidden(Object content) { return create(HttpStatusCode.Forbidden, content); }
    public HttpResponse forbidden(Object content, Map<String, String> headers) { return create(HttpStatusCode.Forbidden, content, null, headers); }

    public HttpResponse notFound() { return create(HttpStatusCode.NotFound); }
    public HttpResponse notFound(Object content) { return create(HttpStatusCode.NotFound, content); }
    public HttpResponse notFound(Object content, Map<String, String> headers) { return create(HttpStatusCode.NotFound, content, null, headers); }

    public HttpResponse methodNotAllowed() { return create(HttpStatusCode.MethodNotAllowed); }
    public HttpResponse methodNotAllowed(Object content) { return create(HttpStatusCode.MethodNotAllowed, content); }
    public HttpResponse methodNotAllowed(Object content, Map<String, String> headers) { return create(HttpStatusCode.MethodNotAllowed, content, null, headers); }

    public HttpResponse notAcceptable() { return create(HttpStatusCode.NotAcceptable); }
    public HttpResponse notAcceptable(Object content) { return create(HttpStatusCode.NotAcceptable, content); }
    public HttpResponse notAcceptable(Object content, Map<String, String> headers) { return create(HttpStatusCode.NotAcceptable, content, null, headers); }

    public HttpResponse proxyAuthenticationRequired() { return create(HttpStatusCode.ProxyAuthenticationRequired); }
    public HttpResponse proxyAuthenticationRequired(Object content) { return create(HttpStatusCode.ProxyAuthenticationRequired, content); }
    public HttpResponse proxyAuthenticationRequired(Object content, Map<String, String> headers) { return create(HttpStatusCode.ProxyAuthenticationRequired, content, null, headers); }

    public HttpResponse requestTimeout() { return create(HttpStatusCode.RequestTimeout); }
    public HttpResponse requestTimeout(Object content) { return create(HttpStatusCode.RequestTimeout, content); }
    public HttpResponse requestTimeout(Object content, Map<String, String> headers) { return create(HttpStatusCode.RequestTimeout, content, null, headers); }

    public HttpResponse conflict() { return create(HttpStatusCode.Conflict); }
    public HttpResponse conflict(Object content) { return create(HttpStatusCode.Conflict, content); }
    public HttpResponse conflict(Object content, Map<String, String> headers) { return create(HttpStatusCode.Conflict, content, null, headers); }

    public HttpResponse gone() { return create(HttpStatusCode.Gone); }
    public HttpResponse gone(Object content) { return create(HttpStatusCode.Gone, content); }
    public HttpResponse gone(Object content, Map<String, String> headers) { return create(HttpStatusCode.Gone, content, null, headers); }

    public HttpResponse lengthRequired() { return create(HttpStatusCode.LengthRequired); }
    public HttpResponse lengthRequired(Object content) { return create(HttpStatusCode.LengthRequired, content); }
    public HttpResponse lengthRequired(Object content, Map<String, String> headers) { return create(HttpStatusCode.LengthRequired, content, null, headers); }

    public HttpResponse preconditionFailed() { return create(HttpStatusCode.PreconditionFailed); }
    public HttpResponse preconditionFailed(Object content) { return create(HttpStatusCode.PreconditionFailed, content); }
    public HttpResponse preconditionFailed(Object content, Map<String, String> headers) { return create(HttpStatusCode.PreconditionFailed, content, null, headers); }

    public HttpResponse contentTooLarge() { return create(HttpStatusCode.ContentTooLarge); }
    public HttpResponse contentTooLarge(Object content) { return create(HttpStatusCode.ContentTooLarge, content); }
    public HttpResponse contentTooLarge(Object content, Map<String, String> headers) { return create(HttpStatusCode.ContentTooLarge, content, null, headers); }

    public HttpResponse uriTooLong() { return create(HttpStatusCode.URITooLong); }
    public HttpResponse uriTooLong(Object content) { return create(HttpStatusCode.URITooLong, content); }
    public HttpResponse uriTooLong(Object content, Map<String, String> headers) { return create(HttpStatusCode.URITooLong, content, null, headers); }

    public HttpResponse unsupportedMediaType() { return create(HttpStatusCode.UnsupportedMediaType); }
    public HttpResponse unsupportedMediaType(Object content) { return create(HttpStatusCode.UnsupportedMediaType, content); }
    public HttpResponse unsupportedMediaType(Object content, Map<String, String> headers) { return create(HttpStatusCode.UnsupportedMediaType, content, null, headers); }

    public HttpResponse rangeNotSatisfiable() { return create(HttpStatusCode.RangeNotSatisfiable); }
    public HttpResponse rangeNotSatisfiable(Object content) { return create(HttpStatusCode.RangeNotSatisfiable, content); }
    public HttpResponse rangeNotSatisfiable(Object content, Map<String, String> headers) { return create(HttpStatusCode.RangeNotSatisfiable, content, null, headers); }

    public HttpResponse expectationFailed() { return create(HttpStatusCode.ExpectationFailed); }
    public HttpResponse expectationFailed(Object content) { return create(HttpStatusCode.ExpectationFailed, content); }
    public HttpResponse expectationFailed(Object content, Map<String, String> headers) { return create(HttpStatusCode.ExpectationFailed, content, null, headers); }

    public HttpResponse imATeapot() { return create(HttpStatusCode.ImATeapot); }
    public HttpResponse imATeapot(Object content) { return create(HttpStatusCode.ImATeapot, content); }
    public HttpResponse imATeapot(Object content, Map<String, String> headers) { return create(HttpStatusCode.ImATeapot, content, null, headers); }

    public HttpResponse misdirectedRequest() { return create(HttpStatusCode.MisdirectedRequest); }
    public HttpResponse misdirectedRequest(Object content) { return create(HttpStatusCode.MisdirectedRequest, content); }
    public HttpResponse misdirectedRequest(Object content, Map<String, String> headers) { return create(HttpStatusCode.MisdirectedRequest, content, null, headers); }

    public HttpResponse unprocessableContent() { return create(HttpStatusCode.UnprocessableContent); }
    public HttpResponse unprocessableContent(Object content) { return create(HttpStatusCode.UnprocessableContent, content); }
    public HttpResponse unprocessableContent(Object content, Map<String, String> headers) { return create(HttpStatusCode.UnprocessableContent, content, null, headers); }

    public HttpResponse locked() { return create(HttpStatusCode.Locked); }
    public HttpResponse locked(Object content) { return create(HttpStatusCode.Locked, content); }
    public HttpResponse locked(Object content, Map<String, String> headers) { return create(HttpStatusCode.Locked, content, null, headers); }

    public HttpResponse failedDependency() { return create(HttpStatusCode.FailedDependency); }
    public HttpResponse failedDependency(Object content) { return create(HttpStatusCode.FailedDependency, content); }
    public HttpResponse failedDependency(Object content, Map<String, String> headers) { return create(HttpStatusCode.FailedDependency, content, null, headers); }

    public HttpResponse tooEarly() { return create(HttpStatusCode.TooEarly); }
    public HttpResponse tooEarly(Object content) { return create(HttpStatusCode.TooEarly, content); }
    public HttpResponse tooEarly(Object content, Map<String, String> headers) { return create(HttpStatusCode.TooEarly, content, null, headers); }

    public HttpResponse upgradeRequired() { return create(HttpStatusCode.UpgradeRequired); }
    public HttpResponse upgradeRequired(Object content) { return create(HttpStatusCode.UpgradeRequired, content); }
    public HttpResponse upgradeRequired(Object content, Map<String, String> headers) { return create(HttpStatusCode.UpgradeRequired, content, null, headers); }

    public HttpResponse preconditionRequired() { return create(HttpStatusCode.PreconditionRequired); }
    public HttpResponse preconditionRequired(Object content) { return create(HttpStatusCode.PreconditionRequired, content); }
    public HttpResponse preconditionRequired(Object content, Map<String, String> headers) { return create(HttpStatusCode.PreconditionRequired, content, null, headers); }

    public HttpResponse tooManyRequests() { return create(HttpStatusCode.TooManyRequests); }
    public HttpResponse tooManyRequests(Object content) { return create(HttpStatusCode.TooManyRequests, content); }
    public HttpResponse tooManyRequests(Object content, Map<String, String> headers) { return create(HttpStatusCode.TooManyRequests, content, null, headers); }

    public HttpResponse requestHeaderFieldsTooLarge() { return create(HttpStatusCode.RequestHeaderFieldsTooLarge); }
    public HttpResponse requestHeaderFieldsTooLarge(Object content) { return create(HttpStatusCode.RequestHeaderFieldsTooLarge, content); }
    public HttpResponse requestHeaderFieldsTooLarge(Object content, Map<String, String> headers) { return create(HttpStatusCode.RequestHeaderFieldsTooLarge, content, null, headers); }

    public HttpResponse unavailableForLegalReasons() { return create(HttpStatusCode.UnavailableForLegalReasons); }
    public HttpResponse unavailableForLegalReasons(Object content) { return create(HttpStatusCode.UnavailableForLegalReasons, content); }
    public HttpResponse unavailableForLegalReasons(Object content, Map<String, String> headers) { return create(HttpStatusCode.UnavailableForLegalReasons, content, null, headers); }

    // 5xx Server Error
    public HttpResponse internalServerError() { return create(HttpStatusCode.InternalServerError); }
    public HttpResponse internalServerError(Object content) { return create(HttpStatusCode.InternalServerError, content); }
    public HttpResponse internalServerError(Object content, Map<String, String> headers) { return create(HttpStatusCode.InternalServerError, content, null, headers); }

    public HttpResponse notImplemented() { return create(HttpStatusCode.NotImplemented); }
    public HttpResponse notImplemented(Object content) { return create(HttpStatusCode.NotImplemented, content); }
    public HttpResponse notImplemented(Object content, Map<String, String> headers) { return create(HttpStatusCode.NotImplemented, content, null, headers); }

    public HttpResponse badGateway() { return create(HttpStatusCode.BadGateway); }
    public HttpResponse badGateway(Object content) { return create(HttpStatusCode.BadGateway, content); }
    public HttpResponse badGateway(Object content, Map<String, String> headers) { return create(HttpStatusCode.BadGateway, content, null, headers); }

    public HttpResponse serviceUnavailable() { return create(HttpStatusCode.ServiceUnavailable); }
    public HttpResponse serviceUnavailable(Object content) { return create(HttpStatusCode.ServiceUnavailable, content); }
    public HttpResponse serviceUnavailable(Object content, Map<String, String> headers) { return create(HttpStatusCode.ServiceUnavailable, content, null, headers); }

    public HttpResponse gatewayTimeout() { return create(HttpStatusCode.GatewayTimeout); }
    public HttpResponse gatewayTimeout(Object content) { return create(HttpStatusCode.GatewayTimeout, content); }
    public HttpResponse gatewayTimeout(Object content, Map<String, String> headers) { return create(HttpStatusCode.GatewayTimeout, content, null, headers); }

    public HttpResponse httpVersionNotSupported() { return create(HttpStatusCode.HttpVersionNotSupported); }
    public HttpResponse httpVersionNotSupported(Object content) { return create(HttpStatusCode.HttpVersionNotSupported, content); }
    public HttpResponse httpVersionNotSupported(Object content, Map<String, String> headers) { return create(HttpStatusCode.HttpVersionNotSupported, content, null, headers); }

    public HttpResponse variantAlsoNegotiates() { return create(HttpStatusCode.VariantAlsoNegotiates); }
    public HttpResponse variantAlsoNegotiates(Object content) { return create(HttpStatusCode.VariantAlsoNegotiates, content); }
    public HttpResponse variantAlsoNegotiates(Object content, Map<String, String> headers) { return create(HttpStatusCode.VariantAlsoNegotiates, content, null, headers); }

    public HttpResponse insufficientStorage() { return create(HttpStatusCode.InsufficientStorage); }
    public HttpResponse insufficientStorage(Object content) { return create(HttpStatusCode.InsufficientStorage, content); }
    public HttpResponse insufficientStorage(Object content, Map<String, String> headers) { return create(HttpStatusCode.InsufficientStorage, content, null, headers); }

    public HttpResponse loopDetected() { return create(HttpStatusCode.LoopDetected); }
    public HttpResponse loopDetected(Object content) { return create(HttpStatusCode.LoopDetected, content); }
    public HttpResponse loopDetected(Object content, Map<String, String> headers) { return create(HttpStatusCode.LoopDetected, content, null, headers); }

    public HttpResponse notExtended() { return create(HttpStatusCode.NotExtended); }
    public HttpResponse notExtended(Object content) { return create(HttpStatusCode.NotExtended, content); }
    public HttpResponse notExtended(Object content, Map<String, String> headers) { return create(HttpStatusCode.NotExtended, content, null, headers); }

    public HttpResponse networkAuthenticationRequired() { return create(HttpStatusCode.NetworkAuthenticationRequired); }
    public HttpResponse networkAuthenticationRequired(Object content) { return create(HttpStatusCode.NetworkAuthenticationRequired, content); }
    public HttpResponse networkAuthenticationRequired(Object content, Map<String, String> headers) { return create(HttpStatusCode.NetworkAuthenticationRequired, content, null, headers); }

    private HttpResponse create(HttpStatusCode statusCode) {
        return create(statusCode, null, null, null);
    }

    private HttpResponse create(HttpStatusCode statusCode, Object content) {
        return create(statusCode, content, null, null);
    }

    private HttpResponse create(HttpStatusCode statusCode, Object content, String location) {
        return create(statusCode, content, location, null);
    }

    private HttpResponse create(
            HttpStatusCode statusCode,
            Object content,
            String location,
            Map<String, String> othersHeaders
    ) {
        var contentType = "";
        var body = InputStream.nullInputStream();

        if (content != null) {
            var serialized = serializerBody(content);
            contentType = serialized.contentType();
            body = serialized.bodyStream();
        }

        var contentLength = getContentLength(body);
        var headers = getHeaders(contentType, contentLength);

        if (location != null && !location.isEmpty())
            headers.set(HttpHeader.Location, location);

        if (othersHeaders != null && !othersHeaders.isEmpty())
            headers.extend(othersHeaders);

        return new HttpResponse.Builder()
                .withVersion(options.version())
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(body)
                .build();
    }
}
