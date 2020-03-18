package site.yazago.oauth.config;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import site.yazago.oauth.data.User;

import java.util.Map;
import java.util.UUID;

public class CustomJwtAccessTokenConverter extends JwtAccessTokenConverter {


    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        UUID userId = extractUserId(authentication);

        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(
                Map.of("user_id", userId)
        );
        return super.enhance(accessToken, authentication);
    }


    private UUID extractUserId(OAuth2Authentication authentication) {
        return ((User) authentication.getUserAuthentication().getPrincipal()).getId();
    }
}
