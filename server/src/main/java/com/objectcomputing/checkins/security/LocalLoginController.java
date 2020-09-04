package com.objectcomputing.checkins.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.objectcomputing.checkins.services.memberprofile.MemberProfile;
import com.objectcomputing.checkins.services.memberprofile.currentuser.CurrentUserServices;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.Authenticator;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.event.LoginFailedEvent;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.handlers.LoginHandler;
import io.micronaut.security.oauth2.endpoint.token.response.*;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.View;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.Collections;
import java.util.Map;

@Requires(env = "local")
@Controller("/oauth/login/google")
@Secured(SecurityRule.IS_ANONYMOUS)
public class LocalLoginController {

    protected final Authenticator authenticator;
    protected final LoginHandler loginHandler;
    protected final ApplicationEventPublisher eventPublisher;
    private final CurrentUserServices currentUserServices;
    private final OpenIdUserDetailsMapper userDetailMapper;

    /**
     * @param authenticator  {@link Authenticator} collaborator
     * @param loginHandler   A collaborator which helps to build HTTP response depending on success or failure.
     * @param eventPublisher The application event publisher
     * @param currentUserServices Current User services
     * @param userDetailMapper
     */
    public LocalLoginController(Authenticator authenticator,
                                LoginHandler loginHandler,
                                ApplicationEventPublisher eventPublisher,
                                CurrentUserServices currentUserServices, OpenIdUserDetailsMapper userDetailMapper) {
        this.authenticator = authenticator;
        this.loginHandler = loginHandler;
        this.eventPublisher = eventPublisher;
        this.currentUserServices = currentUserServices;
        this.userDetailMapper = userDetailMapper;
    }


    @View("login")
    @Get
    public Map<String, String> login() {
        return Collections.emptyMap();
    }

    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON})
    @Post
    public Single<MutableHttpResponse<?>> auth(HttpRequest<?> request, String email, String role) {
        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(email, role);
        Flowable<AuthenticationResponse> authenticationResponseFlowable =
                Flowable.fromPublisher(authenticator.authenticate(request, usernamePasswordCredentials));

        return authenticationResponseFlowable.map(authenticationResponse -> {
            if (authenticationResponse.isAuthenticated() && authenticationResponse.getUserDetails().isPresent()) {

                UserDetails userDetails = authenticationResponse.getUserDetails().get();
                MemberProfile memberProfile = currentUserServices.findOrSaveUser(email, email);
                UserDetails checkinDeets = userDetailMapper.createUserDetails("provider", new OpenIdTokenResponse(),
                        new JWTOpenIdClaims(mkOpenIdClaims(email, memberProfile.getName(), "")));
                checkinDeets.setRoles(userDetails.getRoles());
                //userDetails.setAttributes(Map.of("email", memberProfile.getWorkEmail(), "name", memberProfile.getName(),
                //        "picture", ""));
                eventPublisher.publishEvent(new LoginSuccessfulEvent(userDetails));
                return loginHandler.loginSuccess(checkinDeets, request);
            } else {
                eventPublisher.publishEvent(new LoginFailedEvent(authenticationResponse));
                return loginHandler.loginFailed(authenticationResponse, request);
            }
        }).first(HttpResponse.status(HttpStatus.UNAUTHORIZED));
    }

    private JWTClaimsSet mkOpenIdClaims(String email, String name, String pic) {
        return new JWTClaimsSet.Builder()
                .claim("email", email)
                .subject(email)
                .claim("username", email)
                .issuer("check-ins")
                .claim("name", name)
                .claim("picture", pic).build();
    }
}