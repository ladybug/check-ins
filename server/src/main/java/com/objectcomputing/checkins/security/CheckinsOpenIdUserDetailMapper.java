package com.objectcomputing.checkins.security;

import com.objectcomputing.checkins.services.memberprofile.MemberProfileRepository;
import com.objectcomputing.checkins.services.role.RoleRepository;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.*;
import io.micronaut.security.token.jwt.generator.claims.JwtClaims;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@Replaces(DefaultOpenIdUserDetailsMapper.class)
public class CheckinsOpenIdUserDetailMapper implements OpenIdUserDetailsMapper {

    @Inject
    private MemberProfileRepository memberProfileRepository;

    @Inject
    private RoleRepository roleRepository;

    @NonNull
    @Override
    public UserDetails createUserDetails(String providerName, OpenIdTokenResponse tokenResponse, OpenIdClaims openIdClaims) {
        Map<String, Object> claims = buildAttributes(providerName, tokenResponse, openIdClaims);
        RelevantClaims rc = getRoles(openIdClaims);
        if (rc.getPdlId() != null) {
            claims.put("pdl", rc.getPdlId().toString());
        }
        if (rc.getTeamMembers() != null && rc.getTeamMembers().size() > 0) {
            claims.put("teamMembers", rc.getTeamMembers().stream().map(memberId -> memberId.toString()).collect(Collectors.toList()));
        }
        String username = openIdClaims.getSubject();
        return new UserDetails(username, rc.getRoles(), claims);
    }

    @NonNull
    @Override
    public AuthenticationResponse createAuthenticationResponse(String providerName, OpenIdTokenResponse tokenResponse, OpenIdClaims openIdClaims, @Nullable State state) {
        return createUserDetails(providerName, tokenResponse, openIdClaims);
    }

    /**
     * @param providerName  The OpenID provider name
     * @param tokenResponse The token response
     * @param openIdClaims  The OpenID claims
     * @return The attributes to set in the {@link UserDetails}
     */
    protected Map<String, Object> buildAttributes(String providerName, OpenIdTokenResponse tokenResponse, OpenIdClaims openIdClaims) {
        Map<String, Object> claims = new HashMap<>(openIdClaims.getClaims());
        JwtClaims.ALL_CLAIMS.forEach(claims::remove);
        claims.put(OauthUserDetailsMapper.PROVIDER_KEY, providerName);
        claims.put(OpenIdUserDetailsMapper.OPENID_TOKEN_KEY, tokenResponse.getIdToken());
        return claims;
    }

    /**
     * @param openIdClaims The OpenID claims
     * @return The roles to set in the {@link UserDetails}
     */
    protected RelevantClaims getRoles(OpenIdClaims openIdClaims) {
        RelevantClaims rc = new RelevantClaims();
        memberProfileRepository.findByWorkEmail(openIdClaims.getEmail())
                .ifPresent((memberProfile) -> {
                    rc.getRoles().addAll(roleRepository.findByMemberid(memberProfile.getUuid())
                            .stream()
                            .map(role -> role.getRole().toString())
                            .collect(Collectors.toList()));
                    rc.setPdlId(memberProfile.getPdlId());
                    if (rc.getPdlId() != null) {
                        memberProfileRepository.findByPdlId(memberProfile.getUuid())
                                .stream()
                                .map((teamMemberProfile) -> teamMemberProfile.getUuid())
                                .collect(Collectors.toList());
                    }
                });

        return rc;
    }


    private class RelevantClaims {
        private List<String> roles;
        private UUID pdlId;
        private List<UUID> teamMembers;

        public List<String> getRoles() {
            if (roles == null) {
                roles = new ArrayList<>();
            }
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public UUID getPdlId() {
            return pdlId;
        }

        public void setPdlId(UUID pdlId) {
            this.pdlId = pdlId;
        }

        public List<UUID> getTeamMembers() {
            if (teamMembers == null) {
                teamMembers = new ArrayList<UUID>();
            }
            return teamMembers;
        }

        public void setTeamMembers(List<UUID> teamMembers) {
            this.teamMembers = teamMembers;
        }
    }
}
